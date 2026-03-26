package com.example.serviciotenico;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    RequestQueue resquestQueue;
    private TextView tv_registrate;
    private TextView tv_bienbenida, tv_info;
    private EditText et_nombre;
    private EditText et_direccion;
    private EditText et_telefono;
    private EditText et_email;
    private EditText et_contrasena;
    private Button btn_eliminar, btn_buscar, btn_editar, btn_guardar, btn_averia, btn_identificate, btn_login, btn_nuevo, btn_llamar;
    private String idRecuperado;
    private String telefonoAdmin = "600000000",  contrasenaAdmin = "1234", contrasenaUsuario;
    private String telefonoAdminDB = "667271845",  contrasenaAdminDB = "1234";

    private boolean admin = false;
    private ImageButton iconoUsuario, iconoAdmin, imageAplicacion;
    private Button btn_volver_mostrarCliente, btn_averiasPendientes, btn_crearAveria;
    JSONObject datosClienteGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        et_nombre = findViewById(R.id.et_nombre);
        et_direccion = findViewById(R.id.et_direccion);
        et_telefono = findViewById(R.id.et_telefono);
        et_email = findViewById(R.id.et_email);
        et_contrasena = findViewById(R.id.et_contrasena);
        tv_bienbenida = findViewById(R.id.tv_bienbenida);
        tv_info = findViewById(R.id.tv_info);
        tv_registrate = findViewById(R.id.tv_registrate);

        btn_eliminar = findViewById(R.id.btn_eliminar);
        btn_buscar = findViewById(R.id.btn_buscar);
        btn_editar = findViewById(R.id.btn_editar);
        btn_guardar = findViewById(R.id.btn_guardar);
        btn_averia = findViewById(R.id.btn_averia);
        btn_identificate = findViewById(R.id.btn_identificate);
        btn_login = findViewById(R.id.btn_login);
        btn_nuevo = findViewById(R.id.btn_nuevo);
        iconoUsuario = findViewById(R.id.iconoUsuario);
        iconoAdmin = findViewById(R.id.iconoAdmin);
        btn_volver_mostrarCliente = findViewById(R.id.btn_volver_mostrarCliente);
        btn_averiasPendientes = findViewById(R.id.btn_averiasPendientes);
        btn_crearAveria = findViewById(R.id.btn_crearAveria);
        btn_llamar = findViewById(R.id.btn_llamar);
        imageAplicacion = findViewById(R.id.imageAplicacion);



        // Inicialmente todo oculto excepto teléfono, contraseña, registrate y identificate
        mostrarCampos(false);
        imageAplicacion.setVisibility(View.VISIBLE);



        //PARA LA VUELTA
        boolean de_vuelta = getIntent().getBooleanExtra("de_vuelta", false);
        if(de_vuelta){
         boolean  esAdmin = getIntent().getBooleanExtra("es_admin", false);
         String  telefono = getIntent().getStringExtra("telefono");
          contrasenaUsuario = getIntent().getStringExtra("contrasena");
         if(esAdmin) {
             //rellena los campos y clic en el boton identificate
             et_telefono.setText(telefonoAdmin);
             et_contrasena.setText(contrasenaAdmin);
             btn_identificate.performClick();
             //una ves identificado obtiene el telefono del cliente rellena el campo y lo busca
             et_telefono.setText(telefono);
             // Si se pasó un teléfono, se busca. Si no, se queda en la pantalla de admin.
             if (telefono != null && !telefono.isEmpty()) {
                btn_buscar.performClick();
             }
         }else{
             //si no es el administrador rellena los campos y lo identifica
             et_telefono.setText(telefono);
             et_contrasena.setText(contrasenaUsuario);
             btn_identificate.performClick();
         }
        }
        //FIN VUELTA
    }

    public void insertarCliente(View view) {
        if (!validarCampos()) return;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://www.serviciotecnicosevilla.com/dbst/insertar_cliente.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s.equalsIgnoreCase("Operacion exitosa")) {
                    Toast.makeText(getApplicationContext(), "Registro completado. Identifícate.", Toast.LENGTH_SHORT).show();

                    if(admin){
                        //btn_buscar.performClick();

                        mostrarAdmin();
                    }else{
                        // MÉTODO PARA VOLVER AL LOGIN
                        mostrarLogin();
                    }


                } else if (s.equalsIgnoreCase("El telefono ya esta registrado")) {
                    Toast.makeText(getApplicationContext(), "Este teléfono pertenece a otro cliente", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    String errorHtml = new String(volleyError.networkResponse.data);
                    Toast.makeText(getApplicationContext(), "Error Servidor: Telefono ya registrado " + errorHtml, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error: " + volleyError.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("nombre", et_nombre.getText().toString());
                parametros.put("direccion", et_direccion.getText().toString());
                parametros.put("telefono", et_telefono.getText().toString());
                parametros.put("email", et_email.getText().toString());
                parametros.put("contrasena", et_contrasena.getText().toString());
                return parametros;
            }
        };

        // No vuelvas a inicializar la cola aquí, usa la del onCreate
        resquestQueue = Volley.newRequestQueue(this);
        resquestQueue.add(stringRequest);
    }


    private boolean validarCampos() {
        String nombre = et_nombre.getText().toString().trim();
        String direccion = et_direccion.getText().toString().trim();
        String telefono = et_telefono.getText().toString().trim();
        String email = et_email.getText().toString().trim();
        String contrasena = et_contrasena.getText().toString().trim();

        if (nombre.isEmpty()) {
            et_nombre.setError("Nombre requerido");
            et_nombre.requestFocus();
            return false;
        }

        if (direccion.isEmpty()) {
            et_direccion.setError("Dirección requerida");
            et_direccion.requestFocus();
            return false;
        }

        if (telefono.length() != 9) {
            et_telefono.setError("El teléfono debe tener exactamente 9 dígitos");
            et_telefono.requestFocus();
            return false;
        }

        if (!telefono.matches("[0-9]+")) {
            et_telefono.setError("El teléfono solo puede contener números");
            et_telefono.requestFocus();
            return false;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_email.setError("Email no válido, Ejemplo: correo@gmail.com");
            et_email.requestFocus();
            return false;
        }

        // Solo validamos contraseña si el campo es visible (para nuevos registros)
        if (et_contrasena.getVisibility() == View.VISIBLE && contrasena.length() < 4) {
            et_contrasena.setError("Contraseña demasiado corta (mín. 4)");
            et_contrasena.requestFocus();
            return false;
        }

        return true; // Si llega aquí, todo es válido
    }



    public void guardarClienteDatoGlovale() {
        // Usamos el teléfono para buscar
        String url = "https://www.serviciotecnicosevilla.com/dbst/buscar_cliente.php?telefono=" + et_telefono.getText().toString();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                if (jsonArray.length() > 0) {
                    try {
                        // Tomamos el primer resultado encontrado
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        datosClienteGlobal = jsonArray.getJSONObject(0);
                        // Guardamos el ID en un campo oculto o variable si lo necesitas para la avería
                        idRecuperado = jsonObject.getString("id_cliente");


                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Cliente no encontrado", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });

        resquestQueue = Volley.newRequestQueue(this);
        resquestQueue.add(jsonArrayRequest);
    }


    public void buscarCliente(View view) {
        String telefono = et_telefono.getText().toString().trim();

        if (telefono.isEmpty()) {
            et_telefono.setError("Introduce un teléfono para buscar");
            et_telefono.requestFocus();
            return;
        }

        if (telefono.length() != 9) {
            et_telefono.setError("El teléfono debe tener exactamente 9 dígitos");
            et_telefono.requestFocus();
            return;
        }

        if (!telefono.matches("[0-9]+")) {
            et_telefono.setError("El teléfono solo puede contener números");
            et_telefono.requestFocus();
            return;
        }

        // Usamos el teléfono para buscar
        String url = "https://www.serviciotecnicosevilla.com/dbst/buscar_cliente.php?telefono=" + telefono;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                if (jsonArray.length() > 0) {
                    try {
                        // Tomamos el primer resultado encontrado
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        datosClienteGlobal = jsonArray.getJSONObject(0);

                        // Rellenamos los campos con los datos de la base de datos
                        et_nombre.setText(jsonObject.getString("nombre"));
                        et_direccion.setText(jsonObject.getString("direccion"));
                        et_email.setText(jsonObject.getString("email"));
                        et_telefono.setText(jsonObject.getString("telefono"));
                        et_contrasena.setText(jsonObject.getString("contrasena"));
                        
                        et_nombre.setVisibility(View.VISIBLE);
                        et_direccion.setVisibility(View.VISIBLE);
                        et_email.setVisibility(View.VISIBLE);
                        et_telefono.setVisibility(View.VISIBLE);
                        et_contrasena.setVisibility(View.VISIBLE);
                        
                        btn_editar.setVisibility(View.VISIBLE);
                        btn_eliminar.setVisibility(View.VISIBLE);
                        btn_averia.setVisibility(View.VISIBLE);
                        btn_nuevo.setVisibility(View.GONE);
                        btn_buscar.setVisibility(View.GONE);
                        btn_login.setVisibility(View.VISIBLE);
                        btn_volver_mostrarCliente.setVisibility(View.VISIBLE);
                        btn_crearAveria.setVisibility(View.VISIBLE);
                        btn_llamar.setVisibility(View.VISIBLE);
                        if (admin) {
                            btn_llamar.setText("Llamar al cliente");
                        }
                        tv_info.setVisibility(View.GONE);
                        
                        // Guardamos el ID en un campo oculto o variable si lo necesitas para la avería
                        idRecuperado = jsonObject.getString("id_cliente");
                        contrasenaUsuario = jsonObject.getString("contrasena"); // Actualizamos la variable de sesión
                        Toast.makeText(getApplicationContext(), "Cliente encontrado", Toast.LENGTH_SHORT).show();

                        if (!admin) {
                            tv_bienbenida.setText("Bienvenido " + jsonObject.getString("nombre"));
                            mostrarCliente(null);
                            btn_nuevo.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Cliente no encontrado", Toast.LENGTH_SHORT).show();


                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });

        resquestQueue = Volley.newRequestQueue(this);
        resquestQueue.add(jsonArrayRequest);
    }


    public void editarCliente(View view) {
        if (!validarCampos()) return;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://www.serviciotecnicosevilla.com/dbst/editar_cliente.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if(s.equalsIgnoreCase("Cliente editado")){
                    Toast.makeText(getApplicationContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                    buscarCliente(null);
                } else if (s.equalsIgnoreCase("El telefono ya existe")) {
                    Toast.makeText(getApplicationContext(), "El nuevo teléfono ya está registrado por otro cliente", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    String errorHtml = new String(volleyError.networkResponse.data);
                    Toast.makeText(getApplicationContext(), "Error Servidor: " + errorHtml, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error: " + volleyError.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();
                
                // Recuperamos el teléfono original de la variable global (antes de la edición)
                String telefonoAntiguo = "";
                try {
                    if (datosClienteGlobal != null) {
                        telefonoAntiguo = datosClienteGlobal.getString("telefono");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                parametros.put("telefono_antiguo", telefonoAntiguo);
                parametros.put("telefono_nuevo", et_telefono.getText().toString());

                // Estos son los nuevos datos a guardar
                parametros.put("nombre", et_nombre.getText().toString());
                parametros.put("direccion", et_direccion.getText().toString());
                parametros.put("email", et_email.getText().toString());
                parametros.put("contrasena", et_contrasena.getText().toString()); // Enviamos la nueva contraseña

                return parametros;
            }
        };
        resquestQueue = Volley.newRequestQueue(this);
        resquestQueue.add(stringRequest);
    }


    public void eliminarCliente(View view) {

        // 1. Crear el cuadro de diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar eliminación");
        builder.setMessage("¿Estás seguro de que deseas eliminar este cliente? Esta acción no se puede deshacer.");
        builder.setIcon(android.R.drawable.ic_menu_delete);

        // 2. Configurar el botón de "Sí, eliminar"
        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://www.serviciotecnicosevilla.com/dbst/eliminar_cliente.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s.equalsIgnoreCase("Cliente eliminado")) {
                    Toast.makeText(getApplicationContext(), "Cuenta eliminada con éxito", Toast.LENGTH_SHORT).show();
                    if (admin) {
                        mostrarAdmin(); // Si es admin, vuelve a la pantalla de admin
                    } else {
                        mostrarLogin(); // Si es cliente, cierra sesión y va al login
                    }


                } else {
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();
                // Usamos el teléfono para identificar al cliente a borrar
                parametros.put("telefono", et_telefono.getText().toString());
                return parametros;
            }
        };
        //resquestQueue = Volley.newRequestQueue(this)
                resquestQueue = Volley.newRequestQueue(getApplicationContext());
        resquestQueue.add(stringRequest);

    }
});
        // 3. Configurar el botón de "Cancelar"
        builder.setNegativeButton("Cancelar", null);

// 4. Mostrar el diálogo en pantalla
    builder.show();
}



    private void limpiarFormulario(){
        et_nombre.setText("");
        et_direccion.setText("");
        et_telefono.setText("");
        et_email.setText("");
        et_contrasena.setText("");
        tv_bienbenida.setText("");
        tv_info.setText("");
    }


    public void irAAverias(View view) {
        if (idRecuperado == null || datosClienteGlobal == null) {
            Toast.makeText(this, "Primero debe identificarse", Toast.LENGTH_LONG).show();
            return;
        }

        // CAMBIO: Ahora vamos primero al Historial
        Intent intent = new Intent(this, activity_historial.class);
        intent.putExtra("busqueda", "cliente"); // Indicamos que busque por cliente
        intent.putExtra("id_cliente", idRecuperado);
        intent.putExtra("esAdmin", admin); // OJO: Historial usa "esAdmin" (sin guion bajo)
        intent.putExtra("contrasena", contrasenaUsuario);

        try {
            intent.putExtra("nombre", datosClienteGlobal.getString("nombre"));
            intent.putExtra("telefono", datosClienteGlobal.getString("telefono"));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al obtener datos del cliente", Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(intent);

    }




    private void mostrarCampos(boolean mostrar) {
        int visibilidad = mostrar ? View.VISIBLE : View.GONE;
        int estadoInverso = mostrar ? View.GONE : View.VISIBLE;

        // Campos a mostrar/ocultar
        et_nombre.setVisibility(visibilidad);
        et_direccion.setVisibility(visibilidad);
        et_email.setVisibility(visibilidad);

        // Botones de operaciones
        btn_guardar.setVisibility(visibilidad);
        btn_editar.setVisibility(visibilidad);
        btn_eliminar.setVisibility(visibilidad);
        btn_averia.setVisibility(visibilidad);
        btn_buscar.setVisibility(visibilidad);
        btn_login.setVisibility(visibilidad);
        btn_nuevo.setVisibility(visibilidad);
        iconoUsuario.setVisibility(visibilidad);
        iconoAdmin.setVisibility(visibilidad);
        btn_volver_mostrarCliente.setVisibility(visibilidad);
        btn_averiasPendientes.setVisibility(visibilidad);
        btn_crearAveria.setVisibility(visibilidad);
        btn_llamar.setVisibility(visibilidad);

        // Botones iniciales (se ocultan cuando entramos al formulario)
        btn_identificate.setVisibility(estadoInverso);
        tv_registrate.setVisibility(estadoInverso);
        et_telefono.setVisibility(estadoInverso);
        et_contrasena.setVisibility(estadoInverso);
        tv_info.setText("Login");
    }

    private void mostrarLogin(){
        limpiarFormulario();
        imageAplicacion.setVisibility(View.VISIBLE);
        et_telefono.setVisibility(View.VISIBLE);
        et_contrasena.setVisibility(View.VISIBLE);
        et_telefono.setEnabled(true);
        et_contrasena.setEnabled(true);

        tv_registrate.setVisibility(View.VISIBLE);
        btn_identificate.setVisibility(View.VISIBLE);
        btn_guardar.setVisibility(View.GONE);
        btn_login.setVisibility(View.GONE);

        btn_buscar.setVisibility(View.GONE);
        btn_editar.setVisibility(View.GONE);
        btn_eliminar.setVisibility(View.GONE);
        btn_averia.setVisibility(View.GONE);
        btn_nuevo.setVisibility(View.GONE);

        btn_averiasPendientes.setVisibility(View.GONE);
        btn_crearAveria.setVisibility(View.GONE);
        btn_llamar.setVisibility(View.GONE);

        et_nombre.setVisibility(View.GONE);
        et_direccion.setVisibility(View.GONE);
        et_email.setVisibility(View.GONE);

        iconoUsuario.setVisibility(View.GONE);
        iconoAdmin.setVisibility(View.GONE);
        btn_volver_mostrarCliente.setVisibility(View.GONE);
        tv_info.setText("Login");
    }

    private void mostrarRegistro(){
        limpiarFormulario();
        imageAplicacion.setVisibility(View.GONE);
        et_nombre.setVisibility(View.VISIBLE);
        et_direccion.setVisibility(View.VISIBLE);
        et_telefono.setVisibility(View.VISIBLE);
        et_email.setVisibility(View.VISIBLE);
        et_contrasena.setVisibility(View.VISIBLE);

        et_nombre.setEnabled(true);
        et_direccion.setEnabled(true);
        et_telefono.setEnabled(true);
        et_email.setEnabled(true);
        et_contrasena.setEnabled(true);

        btn_guardar.setVisibility(View.VISIBLE);
        btn_guardar.setEnabled(true);
        btn_login.setVisibility(View.VISIBLE);

        tv_registrate.setVisibility(View.GONE);
        btn_identificate.setVisibility(View.GONE);

        btn_editar.setVisibility(View.GONE);
        btn_eliminar.setVisibility(View.GONE);
        btn_averia.setVisibility(View.GONE);
        btn_buscar.setVisibility(View.GONE);
        btn_nuevo.setVisibility(View.GONE);
        btn_llamar.setVisibility(View.GONE);
        btn_login.setText("Ir Login");
        tv_info.setText("Registro");
    }

    public void clickRegistrate(View view) {
        limpiarFormulario();
        mostrarRegistro();
    }


    public void volverLogin(View view) {
        limpiarFormulario();
        admin = false;
        mostrarLogin();

    }

    public void nuevoCliente(View view){
        btn_guardar.setVisibility(View.VISIBLE);
        et_nombre.setText("");
        et_direccion.setText("");
        et_telefono.setText("");
        et_email.setText("");
        et_contrasena.setText("");

        et_nombre.setVisibility(View.VISIBLE);
        et_direccion.setVisibility(View.VISIBLE);
        et_email.setVisibility(View.VISIBLE);
        et_telefono.setVisibility(View.VISIBLE);
        et_contrasena.setVisibility(View.VISIBLE);

        btn_nuevo.setVisibility(View.GONE);
        btn_buscar.setVisibility(View.GONE);
        btn_editar.setVisibility(View.GONE);
        btn_eliminar.setVisibility(View.GONE);
        btn_averia.setVisibility(View.GONE);
        btn_login.setVisibility(View.VISIBLE);
        btn_volver_mostrarCliente.setVisibility(View.VISIBLE);
        btn_averiasPendientes.setVisibility(View.GONE);
        btn_llamar.setVisibility(View.GONE);
        tv_info.setVisibility(View.GONE);




    }

    private void mostrarAdmin(){
        et_telefono.setVisibility(View.VISIBLE);
        et_telefono.setEnabled(true);
        et_telefono.setText("");
        imageAplicacion.setVisibility(View.GONE);
        tv_bienbenida.setText("Bienvenido Administrador");
        tv_info.setText("Busca cliente por telefono");
        tv_info.setVisibility(View.VISIBLE);
        btn_buscar.setVisibility(View.VISIBLE);
        btn_nuevo.setVisibility(View.VISIBLE);
        btn_login.setVisibility(View.VISIBLE);
        btn_averiasPendientes.setVisibility(View.VISIBLE);
        btn_crearAveria.setVisibility(View.GONE);
        btn_llamar.setVisibility(View.GONE);

        et_nombre.setVisibility(View.GONE);
        et_direccion.setVisibility(View.GONE);
        et_email.setVisibility(View.GONE);
        et_contrasena.setVisibility(View.GONE);

        btn_identificate.setVisibility(View.GONE);
        tv_registrate.setVisibility(View.GONE);
        btn_editar.setVisibility(View.GONE);
        btn_eliminar.setVisibility(View.GONE);
        btn_averia.setVisibility(View.GONE);
        btn_guardar.setVisibility(View.GONE);
        btn_volver_mostrarCliente.setVisibility(View.GONE);

        iconoAdmin.setVisibility(View.VISIBLE);
        iconoUsuario.setVisibility(View.GONE);

    }

    public void irANuevaAveria(View view) {
        if (idRecuperado == null || datosClienteGlobal == null) {
            Toast.makeText(this, "Primero debe identificarse", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, AveriaActivity.class);
        intent.putExtra("id_cliente", idRecuperado);
        intent.putExtra("es_admin", admin);
        intent.putExtra("contrasena", contrasenaUsuario);
        intent.putExtra("busqueda", "cliente"); // Para que al volver sepa ir al historial

        try {
            intent.putExtra("nombre", datosClienteGlobal.getString("nombre"));
            intent.putExtra("telefono", datosClienteGlobal.getString("telefono"));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al obtener datos del cliente", Toast.LENGTH_SHORT).show();
            return;
        }


        // Indicamos explícitamente que es una nueva avería
        intent.putExtra("nueva_averia", true);
        intent.putExtra("existe_averia", false);
        intent.putExtra("estado", "Pendiente");

        startActivity(intent);
    }

    public void verAveriasPendientes(View view) {
        if (!admin) {
            Toast.makeText(this, "Acceso solo para administradores", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, activity_historial.class);
        intent.putExtra("busqueda", "pendiente");
        intent.putExtra("esAdmin", admin);
        intent.putExtra("telefono", et_telefono.getText().toString());
        intent.putExtra("contrasena", et_contrasena.getText().toString());

        startActivity(intent);
    }

    public void clickIdentificate(View view) {
        admin = false;
        String tel = et_telefono.getText().toString();
        if (tel.isEmpty()) {
            Toast.makeText(this, "Introduce un teléfono", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tel.length() != 9) {
            et_telefono.setError("El teléfono debe tener exactamente 9 dígitos");
            et_telefono.requestFocus();
            return;
        }

        if (!tel.matches("[0-9]+")) {
            et_telefono.setError("El teléfono solo puede contener números");
            et_telefono.requestFocus();
            return;
        }


        String contra = et_contrasena.getText().toString();
        if(contra.isEmpty()) {
            Toast.makeText(this, "Introduce una contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if(tel.equals(telefonoAdmin)){
            if (contra.equals(contrasenaAdmin)) {
                admin = true;
                mostrarAdmin();
                return;
            }else{
                Toast.makeText(this, "Contraseña no valida ", Toast.LENGTH_SHORT).show();
                et_contrasena.setText("");
                return;
            }
        }


        String url = "https://www.serviciotecnicosevilla.com/dbst/buscar_cliente.php?telefono=" + tel;

        JsonArrayRequest request = new JsonArrayRequest(url, response -> {
            if (response.length() > 0) {
                try {
                        JSONObject obj = response.getJSONObject(0);

                    //ADMIN EN BASE DE DATOS
                    if(telefonoAdminDB.equals(obj.getString("telefono"))){
                        if (contrasenaAdminDB.equals(obj.getString("contrasena"))) {
                            admin = true;
                            mostrarAdmin();
                            return;
                        }else{
                            Toast.makeText(this, "Contraseña no valida ", Toast.LENGTH_SHORT).show();
                            et_contrasena.setText("");
                            return;
                        }
                    }

                    String telefono = obj.getString("telefono");
                    contrasenaUsuario = obj.getString("contrasena");
                    if(!tel.equals(telefono) || !contra.equals(contrasenaUsuario)){
                        Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                        et_contrasena.setText("");
                        return;
                    }
                    //fin validar

                    //UNA VEZ AUTENTICADO GUARDA LOS DATOS DEL CLIENTE EN VARIABLES GLOVALES
                    // guardarClienteDatoGlovale();
                    datosClienteGlobal = response.getJSONObject(0);
                    idRecuperado = obj.getString("id_cliente");

                    //mostrar icono usuario y desde este mostrar los datos y botones
                    iconoUsuario.setVisibility(View.VISIBLE);
                   // mostrarCliente();
                    ocultarLogin();

                    tv_bienbenida.setText("Bienvenido " + obj.getString("nombre"));
                    //Toast.makeText(this, "Bienvenido " + obj.getString("nombre"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Cliente no encontrado. Por favor, regístrate.", Toast.LENGTH_LONG).show();
            }
        }, error -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }

    private void ocultarLogin(){
        et_telefono.setVisibility(View.GONE);
        et_contrasena.setVisibility(View.GONE);
        btn_identificate.setVisibility(View.GONE);
        tv_registrate.setVisibility(View.GONE);
        imageAplicacion.setVisibility(View.GONE);
        btn_averia.setVisibility(View.VISIBLE);
        btn_crearAveria.setVisibility(View.VISIBLE);
        btn_averiasPendientes.setVisibility(View.GONE);
        btn_llamar.setVisibility(View.VISIBLE);
        btn_llamar.setText("Llamar al técnico");
        iconoAdmin.setVisibility(View.GONE);
        tv_info.setText("");
    }

    public void ocultar_cliente(View view){
        if(admin){
            mostrarAdmin();
        }else {
            et_nombre.setVisibility(View.GONE);
            et_direccion.setVisibility(View.GONE);
            et_email.setVisibility(View.GONE);
            et_telefono.setVisibility(View.GONE);
            et_contrasena.setVisibility(View.GONE);

            btn_editar.setVisibility(View.GONE);
            btn_eliminar.setVisibility(View.GONE);

            btn_averia.setVisibility(View.VISIBLE);
            btn_login.setVisibility(View.GONE);
            btn_volver_mostrarCliente.setVisibility(View.GONE);
            btn_crearAveria.setVisibility(View.VISIBLE);
            btn_llamar.setVisibility(View.VISIBLE);
            btn_llamar.setText("Llamar al técnico");
            tv_info.setText("");
        }
    }

    public void mostrarCliente(View view){
        try{
            et_nombre.setText(datosClienteGlobal.getString("nombre"));
            et_direccion.setText(datosClienteGlobal.getString("direccion"));
            et_email.setText(datosClienteGlobal.getString("email"));
            et_telefono.setText(datosClienteGlobal.getString("telefono"));
            et_contrasena.setText(datosClienteGlobal.getString("contrasena"));

            //no puede modificar el telefono
            et_nombre.setEnabled(true);
            et_direccion.setEnabled(true);
            et_email.setEnabled(true);
            et_telefono.setEnabled(false);
            et_contrasena.setEnabled(true);

            et_nombre.setVisibility(View.VISIBLE);
            et_direccion.setVisibility(View.VISIBLE);
            et_email.setVisibility(View.VISIBLE);
            et_telefono.setVisibility(View.VISIBLE);
            et_contrasena.setVisibility(View.VISIBLE);

            //botones
            btn_buscar.setVisibility(View.GONE);
            btn_guardar.setVisibility(View.GONE);
            btn_identificate.setVisibility(View.GONE);
            tv_registrate.setVisibility(View.GONE);
            btn_editar.setVisibility(View.VISIBLE);
            btn_eliminar.setVisibility(View.VISIBLE);
            btn_averia.setVisibility(View.GONE);
            btn_login.setVisibility(View.VISIBLE);
            btn_volver_mostrarCliente.setVisibility(View.VISIBLE);
            btn_crearAveria.setVisibility(View.GONE);
            btn_llamar.setVisibility(View.VISIBLE);
            btn_llamar.setText("Llamar al técnico");
            iconoAdmin.setVisibility(View.GONE);
            tv_info.setText("Mis datos");
            btn_login.setText("Cerrar Sesion");

        }catch (JSONException e){
            Toast.makeText(this, "Error obteniendo datos glovales.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    public void llamarCliente(View view) {
        String telefono;
        if (admin) {
            telefono = et_telefono.getText().toString();
        } else {
            telefono = telefonoAdminDB;
        }

        if (!telefono.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:" + telefono));
            startActivity(intent);
        } else {
            Toast.makeText(this, "No hay número de teléfono", Toast.LENGTH_SHORT).show();
        }
    }
}
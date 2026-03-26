package com.example.serviciotenico;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AveriaActivity extends AppCompatActivity {

    private EditText et_solucion;
    private EditText et_marca;
    private EditText et_sintoma;

    private EditText et_coste;
    private EditText et_precio;
    private EditText et_estado;
    private EditText et_fecha;

    private String idClienteRecuperado, id_averia, contrasena;
    private String estadoActual;
    private boolean esAdmin;
    private Spinner spinnerElectro;

    private Button btn_guardar2, btn_nuevaAveria, btn_editar2, btn_eliminar2;
    private TextView tv_nombre, tv_telefono, tv_telefono2;
    private TextView etiqueta_importe, etiqueta_coste;
    private boolean esnuevaAveria;
    private boolean existe;
    private Button btn_buscarAveriaCliente, btn_volver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_averia);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        et_solucion = findViewById(R.id.et_solucion);
        et_marca = findViewById(R.id.et_marca);
        et_sintoma = findViewById(R.id.et_sintoma);
        etiqueta_importe = findViewById(R.id.etiqueta_importe);
        etiqueta_coste = findViewById(R.id.etiqueta_coste);
        et_coste = findViewById(R.id.et_coste);
        et_precio = findViewById(R.id.et_precio);
        et_estado = findViewById(R.id.et_estado);
        et_fecha = findViewById(R.id.et_fecha);
        tv_nombre  = findViewById(R.id.tv_nombre2);
        tv_telefono = findViewById(R.id.tv_telefono2);
        btn_guardar2 = findViewById(R.id.btn_guardar2);
        
         spinnerElectro = findViewById(R.id.spinnerElectro);
        btn_editar2 = findViewById(R.id.btn_editar2);
        btn_eliminar2 = findViewById(R.id.btn_eliminar2);
        btn_volver = findViewById(R.id.btn_volver);

        // Configuración del campo fecha para usar Calendario
        et_fecha.setFocusable(false); // Evita que salga el teclado numérico
        et_fecha.setClickable(true);
        et_fecha.setOnClickListener(v -> mostrarCalendario());


        btn_guardar2.setEnabled(false);

        // 1. Creamos la lista de opciones
        List<String> listaAparatos = new ArrayList<>();
        listaAparatos.add("Seleccione aparato...");
        listaAparatos.add("Frigorífico");
        listaAparatos.add("Lavadora");
        listaAparatos.add("Lavavajillas");
        listaAparatos.add("Horno");
        listaAparatos.add("Vitroceramica");
        listaAparatos.add("Aire acondicionado");

        // 2. Creamos el adaptador (el "puente" entre los datos y el diseño)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                listaAparatos
        );

        // 3. Le damos un estilo al desplegable
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 4. ¡Listo! Cargamos los datos en el Spinner
        spinnerElectro.setAdapter(adapter);


        // Recuperar datos básicos del Intent
        idClienteRecuperado = getIntent().getStringExtra("id_cliente");
        esAdmin = getIntent().getBooleanExtra("es_admin", false);
        estadoActual = getIntent().getStringExtra("estado");
        tv_nombre.setText(getIntent().getStringExtra("nombre"));
        tv_telefono.setText(getIntent().getStringExtra("telefono"));
        contrasena = getIntent().getStringExtra("contrasena");
        existe = getIntent().getBooleanExtra("existe_averia", false);

        if (getIntent().getBooleanExtra("nueva_averia", false)) {
            existe = false;
            nuevaAveria();
        }



        // 3. Rellenar campos según si existe avería o no
        if (existe) {
            id_averia = getIntent().getStringExtra("id_averia");
            btn_guardar2.setEnabled(false);

            if(estadoActual.equalsIgnoreCase("Pendiente")){
                btn_editar2.setEnabled(true);
                btn_eliminar2.setEnabled(true);
            }  else if(estadoActual.equalsIgnoreCase("Reparado")){
                btn_editar2.setEnabled(false);
                btn_eliminar2.setEnabled(false);
            }

            // Rellenamos los campos con los datos que vienen del Intent
            et_marca.setText(getIntent().getStringExtra("marca"));
            et_sintoma.setText(getIntent().getStringExtra("sintoma"));
            //obtener fecha corta
            String fechaCompleta = getIntent().getStringExtra("fecha"); //YYYY-MM-DD 00:00:00
            String fechaSoloDia = fechaCompleta.substring(0, 10);
            et_fecha.setText(fechaSoloDia);

            String aparato = getIntent().getStringExtra("electrodomestico");
            seleccionarEnSpinner(spinnerElectro, aparato);
            et_estado.setText(getIntent().getStringExtra("estado"));
            et_solucion.setText(getIntent().getStringExtra("solucion"));
            et_precio.setText(getIntent().getStringExtra("precio"));

            // Solo rellenamos datos económicos si es admin
            if (esAdmin) {
                et_coste.setText(getIntent().getStringExtra("coste"));
                btn_editar2.setEnabled(true);
                btn_eliminar2.setEnabled(true);
            }
        } else {
            // Si es una avería nueva, solo ponemos la fecha de hoy
            String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            et_fecha.setText(hoy);
            et_estado.setText("Pendiente");
            btn_guardar2.setEnabled(true);
            btn_editar2.setEnabled(false);
            btn_eliminar2.setEnabled(false);
        }




        if (idClienteRecuperado == null || idClienteRecuperado.isEmpty()) {
            Toast.makeText(this, "Error: No se recibió ID de cliente", Toast.LENGTH_LONG).show();
            // Opcional: Cerrar la actividad porque sin ID no podemos hacer nada
            // finish();
        }


        // B. Aplicar lógica de de visibilidad de campos en lainterfaz
        configurarPantalla(esAdmin, estadoActual);

        boolean vueltaHistorial = getIntent().getBooleanExtra("vueltaHistorial", false);
        boolean volver = getIntent().getBooleanExtra("volver", false);
        if(volver){
                //btn_volver.performClick();
            volverAUsuario();
            return;
        }

        if(!vueltaHistorial && !esnuevaAveria) {//si no viene de historial y no es nueva averia
            buscarAveriasCliente();
            //btn_buscarAveriaCliente.performClick();
        }
    }

    private void configurarPantalla(boolean admin, String estado) {
        // Si no es admin, ocultamos campos de dinero/solución
        int visibilidadAdmin = admin ? View.VISIBLE : View.GONE;

        // Solución visible para todos (Admin y Cliente)
        et_solucion.setVisibility(View.VISIBLE);

        // Coste solo visible para el administrador
        et_coste.setVisibility(visibilidadAdmin);
        etiqueta_coste.setVisibility(visibilidadAdmin);

        // Importe visible para todos (Admin y Cliente)
        et_precio.setVisibility(View.VISIBLE);
        etiqueta_importe.setVisibility(View.VISIBLE);

        et_fecha.setVisibility(View.VISIBLE);

        if(esAdmin){
            spinnerElectro.setEnabled(true);
            et_marca.setEnabled(true);
            et_sintoma.setEnabled(true);
            et_solucion.setEnabled(true);
            et_coste.setEnabled(true);
            et_precio.setEnabled(true);
            et_estado.setEnabled(true);
            et_fecha.setEnabled(true);
        }else{
            et_solucion.setEnabled(false);
            et_coste.setEnabled(false);
            et_precio.setEnabled(false);
            et_estado.setEnabled(false);
            et_fecha.setEnabled(false);
            if(estado.equalsIgnoreCase("Pendiente")){
                spinnerElectro.setEnabled(true);
                et_marca.setEnabled(true);
                et_sintoma.setEnabled(true);
            }else if (estado.equalsIgnoreCase("Reparado")) {
                spinnerElectro.setEnabled(false);
                et_marca.setEnabled(false);
                et_sintoma.setEnabled(false);
            }
        }
    }


    private void seleccionarEnSpinner(Spinner spinner, String valor) {
        if (valor == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(valor)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void mostrarCalendario() {
        Calendar calendar = Calendar.getInstance();
        
        // Si ya hay una fecha escrita, intentamos iniciar el calendario en esa fecha
        String fechaActual = et_fecha.getText().toString();
        if (!fechaActual.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                calendar.setTime(sdf.parse(fechaActual));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int anio = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Formateamos a YYYY-MM-DD para la base de datos (mes va de 0 a 11, por eso sumamos 1)
            String fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            et_fecha.setText(fechaSeleccionada);
        }, anio, mes, dia);

        datePickerDialog.show();
    }


    public void nuevaAveria(){
        esnuevaAveria = true;
        spinnerElectro.setSelection(0);
        et_marca.setText("");
        et_sintoma.setText("");
        et_solucion.setText("");
        et_coste.setText("");
        et_precio.setText("");
        et_estado.setText("Pendiente");
        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        et_fecha.setText(hoy);
        btn_guardar2.setEnabled(true);
        spinnerElectro.setEnabled(true);
        et_marca.setEnabled(true);
        et_sintoma.setEnabled(true);
        btn_editar2.setEnabled(false);
        btn_eliminar2.setEnabled(false);
    }

    public void insertarAveria(View view) {

        if (!validarCamposAveria()) {
            return; // Si algo falla, el método se detiene aquí
        }

        btn_guardar2.setEnabled(false);

        String url = "https://www.serviciotecnicosevilla.com/dbst/insertar_averia.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Toast.makeText(getApplicationContext(), "Avería registrada correctamente", Toast.LENGTH_SHORT).show();

                esnuevaAveria = false;
                //limpiarFormularioAveria();
                //finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), "Error: " + volleyError.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();

                // 1. Datos obligatorios (Cliente e identificación del aparato)
                parametros.put("id_cliente", idClienteRecuperado); // El ID que pasamos por Intent
                parametros.put("electrodomestico", spinnerElectro.getSelectedItem().toString());
                parametros.put("marca", et_marca.getText().toString());
                parametros.put("sintoma", et_sintoma.getText().toString());

                // 2. Fecha manual (si está vacío el PHP pondrá la actual, pero aquí la mandamos)
                parametros.put("fecha", et_fecha.getText().toString());

                // 3. Si es Admin, mandamos los datos económicos y de estado
                if (esAdmin) {
                    parametros.put("solucion", et_solucion.getText().toString());
                    parametros.put("coste", et_coste.getText().toString());
                    parametros.put("precio", et_precio.getText().toString());
                    parametros.put("estado", et_estado.getText().toString());
                } else {
                    // Si es cliente, por defecto el estado es Pendiente
                    parametros.put("estado", "Pendiente");
                }

                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private boolean validarCamposAveria() {
        // 1. Obtener valores
        String marca = et_marca.getText().toString().trim();
        String sintoma = et_sintoma.getText().toString().trim();
        String fecha = et_fecha.getText().toString().trim();
        String aparato = spinnerElectro.getSelectedItem().toString();
        String estado = et_estado.getText().toString().trim();
        String coste = et_coste.getText().toString().trim();
        String precio = et_precio.getText().toString().trim();
        String solucion = et_solucion.getText().toString().trim();

        // 2. Validar campos obligatorios y Spinner
        if (aparato.equals("Seleccione aparato...")) {
            Toast.makeText(this, "Debe seleccionar un aparato", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (marca.isEmpty()) {
            et_marca.setError("La marca es obligatoria");
            return false;
        }
        if (sintoma.isEmpty()) {
            et_sintoma.setError("Describa el síntoma");
            return false;
        }

        // Si el estado es "Reparado", la solución es obligatoria (sea nueva o edición)
        if (estado.equalsIgnoreCase("Reparado")) {
            if (solucion.isEmpty()) {
                et_solucion.setError("Describa la solución para cerrar la avería");
                et_solucion.requestFocus();
                return false;
            }
        }

        // 3. Validar formato de fecha (AAAA-MM-DD)
        String regexFecha = "^\\d{4}-\\d{2}-\\d{2}$";
        if (!fecha.isEmpty() && !fecha.matches(regexFecha)) {
            et_fecha.setError("Formato incorrecto. Use: AAAA-MM-DD");
            return false;
        }

        // 4. Validar que el estado sea solo Reparado o Pendiente
        // Usamos equalsIgnoreCase para que no importe si escriben en mayúsculas o minúsculas
        if (!estado.equalsIgnoreCase("Reparado") && !estado.equalsIgnoreCase("Pendiente")) {
            et_estado.setError("El estado debe ser 'Reparado' o 'Pendiente'");
            return false;
        }

        // 5. Validar que coste y precio sean números (si no están vacíos)
        try {
            if (!coste.isEmpty()) Double.parseDouble(coste);
            if (!precio.isEmpty()) Double.parseDouble(precio);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Coste y Precio deben ser valores numéricos", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true; // Todo correcto
    }

    public void buscarAveriasCliente() {
        
            Intent intent = new Intent(this, activity_historial.class);
            // 1. Datos básicos
            intent.putExtra("busqueda", "cliente");
            intent.putExtra("esAdmin", esAdmin);

            intent.putExtra("contrasena", contrasena);

            //intent.putExtra("id_cliente", idClienteRecuperado);
            //intent.putExtra("nombre", tv_nombre.getText().toString());
            //intent.putExtra("telefono", tv_telefono.getText().toString());

            datosVolverAtras(intent);
            startActivity(intent);

    }




    private void datosVolverAtras(Intent intent){
        //cliente
        //si va a listar averias es administrador
        //intent.putExtra("es_admin", esAdmin);
        intent.putExtra("id_cliente", idClienteRecuperado);
        intent.putExtra("nombre", tv_nombre.getText().toString());
        intent.putExtra("telefono", tv_telefono.getText().toString());
        intent.putExtra("contrasena", contrasena);
        intent.putExtra("estado", et_estado.getText().toString().trim());
        //averia
        if (existe){
            intent.putExtra("existe_averia", true);
            intent.putExtra("id_averia", id_averia);
            intent.putExtra("electrodomestico", spinnerElectro.getSelectedItem().toString());
            intent.putExtra("marca", et_marca.getText().toString().trim());
            intent.putExtra("sintoma", et_sintoma.getText().toString().trim());
            intent.putExtra("fecha", et_fecha.getText().toString().trim());
            intent.putExtra("solucion", et_solucion.getText().toString().trim());
            intent.putExtra("precio", et_precio.getText().toString().trim());
            intent.putExtra("coste", et_coste.getText().toString().trim());
        }
    }


    public void actualizarAveria(View view) {
        if (!validarCamposAveria()) {
            return; // Si algo falla, el método se detiene aquí
        }

        String url = "https://www.serviciotecnicosevilla.com/dbst/actualizar_averia.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Si el PHP devuelve "Actualizado", avisamos al usuario
                    if (response.equalsIgnoreCase("Actualizado")) {
                        Toast.makeText(this, "Datos guardados con éxito", Toast.LENGTH_SHORT).show();
                        //finish(); // Cerramos la pantalla y volvemos al historial
                    } else {
                        Toast.makeText(this, "Error al guardar: " + response, Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Pasamos el ID que ya tenemos para identificar el registro
                params.put("id_averia", id_averia);
                params.put("electrodomestico", spinnerElectro.getSelectedItem().toString());
                params.put("marca", et_marca.getText().toString());
                params.put("sintoma", et_sintoma.getText().toString());
                params.put("solucion", et_solucion.getText().toString());
                params.put("estado", et_estado.getText().toString());
                params.put("coste", et_coste.getText().toString());
                params.put("precio", et_precio.getText().toString());
                params.put("fecha", et_fecha.getText().toString());
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }


    public void eliminarAveria(View view) {
        // 1. Crear alerta de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar Avería");
        builder.setMessage("¿Estás seguro de que deseas borrar esta avería? Esta acción no se puede deshacer.");

        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            // 2. Si el usuario confirma, enviamos la petición a PHP
            String url = "https://www.serviciotecnicosevilla.com/dbst/eliminar_averia.php";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        if (response.equalsIgnoreCase("Eliminado")) {
                            Toast.makeText(this, "Registro borrado", Toast.LENGTH_SHORT).show();
                            //finish(); // Volver al historial
                            btn_editar2.setEnabled(false);
                            btn_eliminar2.setEnabled(false);
                            limpiarFormularioAveria();
                        } else {
                            Toast.makeText(this, "Error: " + response, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("id_averia", id_averia); // El ID que ya tienes
                    return params;
                }
            };
            Volley.newRequestQueue(this).add(stringRequest);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }


    private void limpiarFormularioAveria() {
        spinnerElectro.setSelection(0); // Vuelve a "Seleccione aparato..."
        et_marca.setText("");
        et_sintoma.setText("");
        et_solucion.setText("");
        et_coste.setText("");
        et_precio.setText("");
        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        et_fecha.setText(hoy);
        et_estado.setText("Pendiente"); // Vuelve a "Pendiente"
    }


    public void volver(View view) {

        boolean vueltaHistorial = getIntent().getBooleanExtra("vueltaHistorial", false);

        if (vueltaHistorial) {
            Intent intent = new Intent(this, activity_historial.class);
            // 1. Datos básicos
            String tipoBusqueda = getIntent().getStringExtra("busqueda");
            intent.putExtra("busqueda", (tipoBusqueda != null) ? tipoBusqueda : "cliente");
            intent.putExtra("esAdmin", esAdmin);
            intent.putExtra("contrasena", contrasena);

            datosVolverAtras(intent);
            startActivity(intent);
            finish();
        } else {
            volverAUsuario();
        }
    }

    public void volverAUsuario() {





        Intent intent = new Intent(this, MainActivity.class);



        intent.putExtra("de_vuelta", true);

        intent.putExtra("es_admin", esAdmin);
        intent.putExtra("telefono", tv_telefono.getText().toString());
        intent.putExtra("contrasena", contrasena);

        startActivity(intent);



    }

}
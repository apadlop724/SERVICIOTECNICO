package com.example.serviciotenico;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class activity_historial extends AppCompatActivity {

    ListView lv_historial;
    ArrayList<JSONObject> listaObjetos = new ArrayList<>();
    ArrayList<String> listaAverias = new ArrayList<>();
    ArrayAdapter<String> adapter;

    TextView tv_nombre2, tv_telefono2, tv_seleccion, tv_historial;
    private String id_cliente, id_averia;
    private String nombre, telefono, contrasena, estado, aparato, marca, sintoma, fechaCompleta, fechaSoloDia, solucion, precio, coste;
    private boolean existe, esAdmin;
    private String tipoBusqueda; // Variable para recordar de dónde venimos
    private String telefonoClienteSeleccionado = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lv_historial = findViewById(R.id.lv_historial);
        tv_nombre2 = findViewById(R.id.tv_nombre2);
        tv_telefono2 = findViewById(R.id.tv_telefono2);
        tv_seleccion = findViewById(R.id.tv_seleccion);
        tv_historial = findViewById(R.id.tv_historial);

        esAdmin = getIntent().getBooleanExtra("esAdmin", false);

        tipoBusqueda = getIntent().getStringExtra("busqueda"); // Guardamos el tipo
        if (tipoBusqueda != null && tipoBusqueda.equals("cliente")) {

            // Obtenemos el ID del cliente que nos pasaron desde la pantalla anterior
            id_cliente = getIntent().getStringExtra("id_cliente");
            nombre = getIntent().getStringExtra("nombre");
            telefono = getIntent().getStringExtra("telefono");
            contrasena = getIntent().getStringExtra("contrasena");

            //id_averia = getIntent().getStringExtra("id_averia");
            obtenerHistorial(id_cliente, nombre, telefono);
        } else if (tipoBusqueda != null && tipoBusqueda.equals("pendiente")) {

            //String estado = getIntent().getStringExtra("estado");
            //id_averia = getIntent().getStringExtra("id_averia");
            obtenerHistorialPendientes();
        }

        //obtener datos para volver atras
        obtenerParaVolverAtras();
    }


    private void obtenerParaVolverAtras(){
        //cliente
        //si esta en esta pagina es administrador
        //boolean esAdmin = getIntent().getBooleanExtra("es_admin", false);
        id_cliente = getIntent().getStringExtra("id_cliente");
        nombre = getIntent().getStringExtra("nombre");
        telefono = getIntent().getStringExtra("telefono");
        contrasena = getIntent().getStringExtra("contrasena");
        estado = getIntent().getStringExtra("estado");
        existe = getIntent().getBooleanExtra("existe_averia", false);

        //averia
        if (existe){
            id_averia = getIntent().getStringExtra("id_averia");
            aparato = getIntent().getStringExtra("electrodomestico");
            marca = getIntent().getStringExtra("marca");
            sintoma = getIntent().getStringExtra("sintoma");
            fechaCompleta = getIntent().getStringExtra("fecha"); //YYYY-MM-DD 00:00:00
            fechaSoloDia = fechaCompleta.substring(0, 10);
            solucion = getIntent().getStringExtra("solucion");
            precio = getIntent().getStringExtra("precio");
            coste = getIntent().getStringExtra("coste");
        }
    }


    private void obtenerHistorial(String id_cliente, String nombre, String telefono) {
        tv_nombre2.setText(nombre);
        tv_telefono2.setText(telefono);
        tv_historial.setText(tv_historial.getText() + " del Cliente");

        String url = "https://www.serviciotecnicosevilla.com/dbst/listar_averias.php?id_cliente=" + id_cliente;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            listaObjetos.add(obj); // Guardamos el JSON completo
                            // Formamos una cadena simple para mostrar
                            String info = obj.getString("fecha").substring(0, 10) + " - " +
                                    obj.getString("electrodomestico") + "\nEstado: " +
                                    obj.getString("estado");
                            listaAverias.add(info);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaAverias) {
                        @Override
                        public View getView(int position, View convertView, android.view.ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView textView = (TextView) view.findViewById(android.R.id.text1);
                            String texto = getItem(position);
                            textView.setTextColor(Color.BLACK); // Texto siempre negro para leer bien
                            if (texto.toLowerCase().contains("estado: pendiente")) {
                                view.setBackgroundColor(Color.parseColor("#FFEBEE")); // Rojo Pastel
                            } else if (texto.toLowerCase().contains("estado: reparado")) {
                                view.setBackgroundColor(Color.parseColor("#e3e7fd")); // Azul Pastel
                            } else {
                                view.setBackgroundColor(Color.WHITE);
                            }
                            return view;
                        }
                    };
                    lv_historial.setAdapter(adapter);
                    mostrarSeleccion();
                    //DetallesAveriaLista();
                },
                error -> Toast.makeText(this, "Error al cargar historial", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }

    private void obtenerHistorialPendientes() {
        tv_nombre2.setText("");
        tv_telefono2.setText("");
        tv_historial.setText(tv_historial.getText() + " Pendientes");

        // Limpiamos las listas antes de empezar para que no se acumulen
        listaObjetos.clear();
        listaAverias.clear();

        String url = "https://www.serviciotecnicosevilla.com/dbst/listar_pendientes.php";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() == 0) {
                        Toast.makeText(this, "No hay averías pendientes", Toast.LENGTH_SHORT).show();
                    }

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            listaObjetos.add(obj);

                            // Mostramos Nombre del cliente, Aparato y Fecha
                            String info = obj.getString("fecha").substring(0, 10) + " - " +
                                    obj.getString("electrodomestico") + "\nEstado: " +
                                    obj.getString("estado") + "\n" +
                                    obj.getString("nombre") + " _ " +
                                    obj.getString("telefono");
                            listaAverias.add(info);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaAverias) {
                        @Override
                        public View getView(int position, View convertView, android.view.ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView textView = (TextView) view.findViewById(android.R.id.text1);
                            String texto = getItem(position);
                            textView.setTextColor(Color.BLACK);
                            if (texto.toLowerCase().contains("estado: pendiente")) {
                                view.setBackgroundColor(Color.parseColor("#FFEBEE")); // Rojo Pastel
                            } else if (texto.toLowerCase().contains("estado: reparado")) {
                                view.setBackgroundColor(Color.parseColor("#e3e7fd")); // Azul Pastel
                            } else {
                                view.setBackgroundColor(Color.WHITE);
                            }
                            return view;
                        }
                    };
                    lv_historial.setAdapter(adapter);

                    // Configuramos el click para que al tocar una, nos lleve a editarla
                    mostrarSeleccion();
                    //DetallesAveriaLista();

                },
                error -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }


    private  void mostrarSeleccion(){

        lv_historial.setOnItemClickListener((parent, view, position, id) -> {
            // 1. Restaurar el color de fondo original de las filas (si no, se quedan blancas o transparentes)
            for (int i = 0; i < parent.getChildCount(); i++) {
                View v = parent.getChildAt(i);
                TextView tv = v.findViewById(android.R.id.text1);
                String txt = tv.getText().toString().toLowerCase();

                if (txt.contains("estado: pendiente")) {
                    v.setBackgroundColor(Color.parseColor("#FFEBEE"));
                } else if (txt.contains("estado: reparado")) {
                    v.setBackgroundColor(Color.parseColor("#e3e7fd")); // Azul Pastel
                } else {
                    v.setBackgroundColor(Color.WHITE);
                }
            }

            // 2. Pintar la fila seleccionada con un color de "selección" (ej. Gris oscuro o Azul fuerte)
            view.setBackgroundColor(Color.parseColor("#E3F2FD")); // Azul selección

            try {
                // Sacamos el objeto JSON correspondiente a la fila pulsada
                JSONObject averiaSeleccionada = listaObjetos.get(position);

                // Guardamos el teléfono del cliente de la avería seleccionada para poder volver
                if (averiaSeleccionada.has("telefono")) {
                    telefonoClienteSeleccionado = averiaSeleccionada.getString("telefono");
                }


                // Usamos variables auxiliares: si el JSON no tiene el dato, usamos el de la variable global
                String nombreMostrar = averiaSeleccionada.has("nombre") ? averiaSeleccionada.getString("nombre") : nombre;
                String telefonoMostrar = averiaSeleccionada.has("telefono") ? averiaSeleccionada.getString("telefono") : telefono;

                String fechaCompleta = averiaSeleccionada.getString("fecha"); //YYYY-MM-DD 00:00:00
                String fechaSoloDia = fechaCompleta.substring(0, 10);

                String datos = "Fecha " + fechaSoloDia + " : " +
                        averiaSeleccionada.getString("estado") + "\n" +
                        nombreMostrar + " : " +
                        telefonoMostrar + "\n" +
                        averiaSeleccionada.getString("electrodomestico") + " " +
                        averiaSeleccionada.getString("marca") + " " +
                        averiaSeleccionada.getString("sintoma") + "\nSolucion: " +
                        averiaSeleccionada.getString("solucion");
                        if(esAdmin){
                            datos += " Coste " +
                                    averiaSeleccionada.getString("coste");
                        }
                        datos +=  " Importe " +
                        averiaSeleccionada.getString("precio") + " ";

                tv_seleccion.setText(datos);
                        tv_seleccion.setText(datos);

                tv_seleccion.setBackgroundColor(Color.parseColor("#E3F2FD")); // Azul Selección
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }


    public void DetallesAveriaLista(View view) {
        Detalles();
        Toast.makeText(this, "Vuelva a seleccionar la averia para editarla", Toast.LENGTH_SHORT).show();
    }


    private void Detalles() {

        lv_historial.setOnItemClickListener((parent, view, position, id) -> {


            try {
                // Sacamos el objeto JSON correspondiente a la fila pulsada
                JSONObject averiaSeleccionada = listaObjetos.get(position);

                // Creamos el Intent para ir a la pantalla de detalles
                Intent intent = new Intent(activity_historial.this, AveriaActivity.class);

                // Pasamos todos los datos necesarios
                //intent.putExtra("id_cliente", id_cliente);
                //intent.putExtra("nombre", tv_nombre2.getText().toString());
                //intent.putExtra("telefono", tv_telefono2.getText().toString());

                //si eta en esta pantalla es administrador
                intent.putExtra("vueltaHistorial", true);
                intent.putExtra("es_admin", esAdmin);
                
                // Hacemos lo mismo para enviar los datos a la siguiente pantalla sin errores
                String idClienteEnvio = averiaSeleccionada.has("id_cliente") ? averiaSeleccionada.getString("id_cliente") : id_cliente;
                String nombreEnvio = averiaSeleccionada.has("nombre") ? averiaSeleccionada.getString("nombre") : nombre;
                String telefonoEnvio = averiaSeleccionada.has("telefono") ? averiaSeleccionada.getString("telefono") : telefono;

                intent.putExtra("id_cliente", idClienteEnvio);
                intent.putExtra("busqueda", tipoBusqueda); // Pasamos el tipo para saber volver
                intent.putExtra("contrasena", contrasena);
                intent.putExtra("nombre", nombreEnvio);
                intent.putExtra("telefono", telefonoEnvio);


                intent.putExtra("estado", averiaSeleccionada.getString("estado"));
                //si esta aqui es que exite averia
                intent.putExtra("existe_averia", true);
                intent.putExtra("id_averia", averiaSeleccionada.getString("id_averia"));
                intent.putExtra("marca", averiaSeleccionada.getString("marca"));
                intent.putExtra("sintoma", averiaSeleccionada.getString("sintoma"));
                intent.putExtra("fecha", averiaSeleccionada.getString("fecha"));
                intent.putExtra("electrodomestico", averiaSeleccionada.getString("electrodomestico"));
                intent.putExtra("solucion", averiaSeleccionada.getString("solucion"));
                intent.putExtra("precio", averiaSeleccionada.getString("precio"));
                intent.putExtra("coste", averiaSeleccionada.getString("coste"));
                intent.putExtra("id_averia", averiaSeleccionada.getString("id_averia"));


                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public void volver(View view){
        // CAMBIO: El botón volver del historial regresa al MAIN
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("de_vuelta", true);
        intent.putExtra("es_admin", esAdmin);
        // Si se seleccionó una avería, usamos su teléfono. Si no, el que ya teníamos.
        if (telefonoClienteSeleccionado != null) {
            intent.putExtra("telefono", telefonoClienteSeleccionado);
        } else {
            intent.putExtra("telefono", telefono);
        }
        intent.putExtra("contrasena", contrasena);
        startActivity(intent);
        finish();
    }

    private void datosVolverAtras(Intent intent){
        //cliente

        //intent.putExtra("vueltaHistorial", true);
        intent.putExtra("contrasena", contrasena);
        intent.putExtra("es_admin", esAdmin);
        intent.putExtra("id_cliente", id_cliente);
        intent.putExtra("nombre", nombre);
        intent.putExtra("telefono", telefono);
        intent.putExtra("estado", estado);
        //averia
        if (existe){
            intent.putExtra("existe_averia", true);
            intent.putExtra("id_averia", id_averia);
            intent.putExtra("electrodomestico", aparato);
            intent.putExtra("marca", marca);
            intent.putExtra("sintoma", sintoma);
            intent.putExtra("fecha", fechaSoloDia);
            intent.putExtra("solucion", solucion);
            intent.putExtra("precio", precio);
            intent.putExtra("coste", coste);
        }
    }

}

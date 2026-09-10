package com.juan.tfgplatform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.juan.tfgplatform.model.Entrega;
import com.juan.tfgplatform.model.Pregunta;
import com.juan.tfgplatform.model.Respuesta;
import com.juan.tfgplatform.repository.EntregaRepository;
import com.juan.tfgplatform.repository.PreguntaRepository;
import com.juan.tfgplatform.repository.RespuestaRepository;

import com.fasterxml.jackson.databind.JsonNode;


import org.springframework.stereotype.Service;
import java.math.BigDecimal;

import java.util.List;
import java.util.Map;

@Service
public class RespuestaService {

    private final RespuestaRepository respuestaRepository;
    private final EntregaRepository entregaRepository;
    private final PreguntaRepository preguntaRepository;

    public RespuestaService(RespuestaRepository respuestaRepository,
                            EntregaRepository entregaRepository,
                            PreguntaRepository preguntaRepository) {
        this.respuestaRepository = respuestaRepository;
        this.entregaRepository = entregaRepository;
        this.preguntaRepository = preguntaRepository;
    }

    public Respuesta crearRespuesta(Long entregaId, Long preguntaId, Respuesta respuesta) {

        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega no encontrada"));

        if (entrega.getEstado() != Entrega.EstadoEntrega.EN_PROGRESO){
            throw new RuntimeException("La entrega ya ha sido enviada y no puede modificarse");
        }

        Pregunta pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> new RuntimeException("Pregunta no encontrada"));

        if (!pregunta.getEjercicio().getId().equals(entrega.getEjercicio().getId())){
            throw new RuntimeException("La pregunta no pertenece a este ejercicio");
        }

        respuesta.setEntrega(entrega);
        respuesta.setPregunta(pregunta);

        if (pregunta.getTipo() == Pregunta.TipoPregunta.NUMERICO) {
            BigDecimal valorCorrecto = pregunta.getValorCorrecto();
            BigDecimal tolerancia = pregunta.getTolerancia();

            BigDecimal respuestaAlumno = new BigDecimal(respuesta.getRespuestaTexto());


            BigDecimal puntuacion;
            if(tolerancia == null){
                if (respuestaAlumno.compareTo(valorCorrecto)==0){
                    puntuacion = pregunta.getPuntuacionMaxima();
                } else{
                    puntuacion = BigDecimal.ZERO;
                }
            } else{
                BigDecimal diferencia = respuestaAlumno.subtract(valorCorrecto).abs();
                // tolerancia es un porcentaje (ej: 5 = ±5% del valor correcto)
                BigDecimal margen = valorCorrecto.abs()
                        .multiply(tolerancia)
                        .divide(java.math.BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);

                if(diferencia.compareTo(margen) <= 0){
                    puntuacion = pregunta.getPuntuacionMaxima();
                }else{
                    puntuacion = BigDecimal.ZERO;
                }
            }
                    respuesta.setPuntuacionObtenida(puntuacion);

        }



        return respuestaRepository.save(respuesta);
    }

 public BigDecimal corregirTabla(Pregunta pregunta, Map<String, String> respuestasAlumno) {

    try {

        ObjectMapper mapper = new ObjectMapper();

        JsonNode config = mapper.readTree(pregunta.getConfiguracionJson());

        int filas = config.get("filas").asInt();
        int columnas = config.get("columnas").asInt();

        JsonNode correctas = config.get("respuestasCorrectas");

        double tolerancia = 0;
        if (config.has("tolerancia")) {
            tolerancia = config.get("tolerancia").asDouble();
        }

        int correctasCount = 0;
        int total = filas * columnas;

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {

                String key = "respuesta_" + pregunta.getId() + "_" + i + "_" + j;
                String valor = respuestasAlumno.get(key);

                if (valor == null || valor.isBlank()) {
                    continue;
                }

                double respuestaAlumno = Double.parseDouble(valor);
                double correcta = correctas.get(i).get(j).asDouble();

                double diferencia = Math.abs(respuestaAlumno - correcta);

                if (diferencia <= tolerancia) {
                    correctasCount++;
                }
            }
        }

        return pregunta.getPuntuacionMaxima()
                .multiply(BigDecimal.valueOf(correctasCount))
                .divide(BigDecimal.valueOf(total), 2, java.math.RoundingMode.HALF_UP);

    } catch (Exception e) {
        e.printStackTrace();
        return BigDecimal.ZERO;
    }
}


  

    public List<Respuesta> listarTodas(){
        return respuestaRepository.findAll();
        }
}
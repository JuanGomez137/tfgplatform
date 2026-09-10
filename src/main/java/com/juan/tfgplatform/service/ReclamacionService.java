package com.juan.tfgplatform.service;

import com.juan.tfgplatform.model.Entrega;
import com.juan.tfgplatform.model.Reclamacion;
import com.juan.tfgplatform.model.Respuesta;
import com.juan.tfgplatform.repository.EntregaRepository;
import com.juan.tfgplatform.repository.ReclamacionRepository;
import com.juan.tfgplatform.repository.RespuestaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReclamacionService {

    private final ReclamacionRepository reclamacionRepository;
    private final EntregaRepository entregaRepository;
    private final RespuestaRepository respuestaRepository;

    public ReclamacionService(ReclamacionRepository reclamacionRepository,
                              EntregaRepository entregaRepository,
                              RespuestaRepository respuestaRepository) {
        this.reclamacionRepository = reclamacionRepository;
        this.entregaRepository = entregaRepository;
        this.respuestaRepository = respuestaRepository;
    }

    public Reclamacion crearReclamacion(Long entregaId, Long respuestaId, String comentario) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega no encontrada"));

        Respuesta respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new RuntimeException("Respuesta no encontrada"));

        if (reclamacionRepository.existsByRespuestaId(respuestaId)) {
            throw new RuntimeException("Ya existe una reclamación para esta respuesta");
        }

        Reclamacion reclamacion = new Reclamacion();
        reclamacion.setComentario(comentario);
        reclamacion.setEntrega(entrega);
        reclamacion.setRespuesta(respuesta);

        entrega.setEstado(Entrega.EstadoEntrega.EN_RECLAMACION);
        entregaRepository.save(entrega);

        return reclamacionRepository.save(reclamacion);
    }

    public Reclamacion resolverReclamacion(Long reclamacionId, BigDecimal nuevaNota, String comentarioProfesor) {
        Reclamacion reclamacion = reclamacionRepository.findById(reclamacionId)
                .orElseThrow(() -> new RuntimeException("Reclamación no encontrada"));

        Respuesta respuesta = reclamacion.getRespuesta();
        respuesta.setPuntuacionObtenida(nuevaNota);
        respuestaRepository.save(respuesta);

        // Recalculate total score for the entrega
        Entrega entrega = reclamacion.getEntrega();
        BigDecimal total = BigDecimal.ZERO;
        for (Respuesta r : respuestaRepository.findByEntregaId(entrega.getId())) {
            if (r.getPuntuacionObtenida() != null) {
                total = total.add(r.getPuntuacionObtenida());
            }
        }
        entrega.setNotaTotal(total);
        entrega.setEstado(Entrega.EstadoEntrega.CORREGIDO_MANUAL);
        entregaRepository.save(entrega);

        reclamacion.setResuelta(true);
        reclamacion.setComentarioProfesor(comentarioProfesor);
        return reclamacionRepository.save(reclamacion);
    }

    public List<Reclamacion> obtenerPorEntrega(Long entregaId) {
        return reclamacionRepository.findByEntregaId(entregaId);
    }

    public List<Reclamacion> obtenerPendientes() {
        return reclamacionRepository.findByResueltaFalse();
    }
}

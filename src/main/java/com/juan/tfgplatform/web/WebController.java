package com.juan.tfgplatform.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juan.tfgplatform.dto.EjercicioRequestDTO;
import com.juan.tfgplatform.dto.GrupoRequestDTO;
import com.juan.tfgplatform.dto.UsuarioRequestDTO;
import com.juan.tfgplatform.model.Ejercicio;
import com.juan.tfgplatform.model.Entrega;
import com.juan.tfgplatform.model.Grupo;
import com.juan.tfgplatform.model.Pregunta;
import com.juan.tfgplatform.model.Reclamacion;
import com.juan.tfgplatform.model.Respuesta;
import com.juan.tfgplatform.model.TokenRecuperacion;
import com.juan.tfgplatform.model.Usuario;
import com.juan.tfgplatform.model.Pregunta.TipoPregunta;
import com.juan.tfgplatform.repository.ReclamacionRepository;
import com.juan.tfgplatform.repository.TokenRecuperacionRepository;
import com.juan.tfgplatform.service.*;

import jakarta.servlet.http.HttpServletRequest;

import com.juan.tfgplatform.repository.UsuarioRepository;
import com.juan.tfgplatform.repository.GrupoRepository;
import com.juan.tfgplatform.repository.EjercicioRepository;
import com.juan.tfgplatform.repository.EntregaRepository;
import com.juan.tfgplatform.repository.PreguntaRepository;
import com.juan.tfgplatform.repository.RespuestaRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;



@Controller
public class WebController {

    private final RespuestaService respuestaService;
    private final RespuestaRepository respuestaRepository;

    private final UsuarioService usuarioService;
    private final GrupoService grupoService;
    private final EjercicioService ejercicioService;
    private final PreguntaService preguntaService;
    private final UsuarioRepository usuarioRepository;
    private final EntregaService entregaService;
    private final EntregaRepository entregaRepository;
    private final GrupoRepository grupoRepository;
    private final PreguntaRepository preguntaRepository;
    private final EjercicioRepository ejercicioRepository;
    private final StorageService storageService;
    private final AiGradingService aiGradingService;
    private final ReclamacionService reclamacionService;
    private final ReclamacionRepository reclamacionRepository;
    private final TokenRecuperacionRepository tokenRecuperacionRepository;
    private final EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${uploads.path:./uploads}")
    private String uploadPath;

    public WebController(
            UsuarioService usuarioService,
            GrupoService grupoService,
            EjercicioService ejercicioService,
            PreguntaService preguntaService,
            UsuarioRepository usuarioRepository,
            EntregaService entregaService,
            EntregaRepository entregaRepository,
            RespuestaService respuestaService,
            RespuestaRepository respuestaRepository,
            GrupoRepository grupoRepository,
            EjercicioRepository ejercicioRepository,
            PreguntaRepository preguntaRepository,
            StorageService storageService,
            AiGradingService aiGradingService,
            ReclamacionService reclamacionService,
            ReclamacionRepository reclamacionRepository,
            TokenRecuperacionRepository tokenRecuperacionRepository,
            EmailService emailService) {
        this.usuarioService = usuarioService;
        this.grupoService = grupoService;
        this.ejercicioService = ejercicioService;
        this.preguntaService = preguntaService;
        this.usuarioRepository = usuarioRepository;
        this.entregaService = entregaService;
        this.respuestaService = respuestaService;
        this.respuestaRepository = respuestaRepository;
        this.entregaRepository = entregaRepository;
        this.grupoRepository = grupoRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.preguntaRepository = preguntaRepository;
        this.storageService = storageService;
        this.aiGradingService = aiGradingService;
        this.reclamacionService = reclamacionService;
        this.reclamacionRepository = reclamacionRepository;
        this.tokenRecuperacionRepository = tokenRecuperacionRepository;
        this.emailService = emailService;
    }

    // ------------------------
    // GENERAL
    // ------------------------

    @GetMapping("/")
    public String inicio(){
        return "inicio";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    // ------------------------
    // REGISTRO
    // ------------------------

    @GetMapping("/registro")
    public String registro(Model model){

        model.addAttribute("usuario", new UsuarioRequestDTO());

        return "registro";
    }

    @PostMapping("/registro")
    public String crearUsuarioWeb(@ModelAttribute UsuarioRequestDTO dto, Model model) {
        try {
            String emailLower = dto.getEmail().toLowerCase().trim();

            

            Usuario usuario = new Usuario();
            usuario.setNombre(dto.getNombre());
            usuario.setEmail(emailLower);
            usuario.setPasswordHash(dto.getPassword());
            usuario.setRol(usuarioService.determinarRolPorEmail(usuario.getEmail()));

            boolean emailActivo = emailService.isEnabled();
            if (emailActivo) {
                usuario.setActivo(false); 
            }

            Usuario usuarioGuardado = usuarioService.crearUsuario(usuario);

            if (emailActivo) {
                // Create confirmation token (24h validity)
                String token = UUID.randomUUID().toString();
                TokenRecuperacion tr = new TokenRecuperacion();
                tr.setUsuario(usuarioGuardado);
                tr.setToken(token);
                tr.setExpiracion(LocalDateTime.now().plusHours(24));
                tr.setUsado(false);
                tr.setTipo(TokenRecuperacion.TipoToken.CONFIRMACION);
                tokenRecuperacionRepository.save(tr);
                try { emailService.enviarEmailConfirmacion(emailLower, token); } catch (Exception ignored) {}
                model.addAttribute("pendienteConfirmacion", true);
                model.addAttribute("usuario", dto);
                return "registro";
            }

            return "redirect:/login?registrado=true";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", dto);
            return "registro";
        }
    }

    @GetMapping("/confirmar-email")
    public String confirmarEmail(@RequestParam String token, Model model) {
        Optional<TokenRecuperacion> trOpt = tokenRecuperacionRepository.findByToken(token);
        if (trOpt.isEmpty()
                || trOpt.get().getUsado()
                || trOpt.get().getTipo() != TokenRecuperacion.TipoToken.CONFIRMACION
                || trOpt.get().getExpiracion().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "El enlace de confirmación no es válido o ha expirado.");
            return "confirmar_email";
        }
        TokenRecuperacion tr = trOpt.get();
        Usuario u = tr.getUsuario();
        u.setActivo(true);
        usuarioRepository.save(u);
        tr.setUsado(true);
        tokenRecuperacionRepository.save(tr);
        model.addAttribute("ok", true);
        return "confirmar_email";
    }

    // ------------------------
    // PANEL PROFESOR
    // ------------------------

    @GetMapping("/profesor")
    public String panelProfesor(){
        return "profesor";
    }

    // ------------------------
    // GRUPOS
    // ------------------------

    @GetMapping("/profesor/grupos")
    public String verGrupos(Model model){

        model.addAttribute("grupos", grupoService.listarTodos());
        model.addAttribute("ejercicios", ejercicioService.listarTodos());

        return "profesor_grupos";
    }

    @GetMapping("/profesor/grupos/crear")
    public String crearGrupoForm(Model model){
        model.addAttribute("grupo", new GrupoRequestDTO());
        return "crear_grupo";
    }

    @PostMapping("/profesor/grupos/crear")
public String crearGrupoWeb(@ModelAttribute GrupoRequestDTO dto, Model model, Authentication authentication){

    try{

        String email = authentication.getName();

        Usuario profesor = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        Grupo grupo = new Grupo();

        grupo.setNombre(dto.getNombre());
        grupo.setDescripcion(dto.getDescripcion());
        grupo.setProfesor(profesor);

        grupoService.crearGrupo(dto.getNombre(),dto.getDescripcion(),profesor.getId());

        return "redirect:/profesor/grupos";

    }catch(RuntimeException e){

        model.addAttribute("error", e.getMessage());
        model.addAttribute("grupo", dto);

        return "crear_grupo";
    }
}

    @GetMapping("/profesor/grupos/{id}")
    public String verGrupo(@PathVariable Long id, Model model){

        try{

            model.addAttribute("grupo", grupoService.obtenerPorId(id));

            return "ver_grupo";

        }catch(RuntimeException e){

            return "redirect:/profesor/grupos";
        }
    }

@PostMapping("/profesor/grupos/{grupoId}/agregar_alumno")
public String agregarAlumnoAGrupo(@PathVariable Long grupoId,
                                  @RequestParam String emails,
                                  Model model){

    try{

        String[] listaEmails = emails.split(",");

        for(String email : listaEmails){

            Usuario alumno = usuarioRepository.findByEmail(email.trim())
                    .orElse(null);

            if(alumno != null){
                grupoService.agregarAlumno(grupoId, alumno.getId());
            }

        }

        return "redirect:/profesor/grupos/" + grupoId;

    }catch(Exception e){

        model.addAttribute("error", "Error al añadir alumnos");

        return "redirect:/profesor/grupos/" + grupoId;
    }
}

@PostMapping("/profesor/grupos/{grupoId}/eliminar_alumno/{alumnoId}")
public String eliminarAlumnoDeGrupo(@PathVariable Long grupoId,
                                    @PathVariable Long alumnoId){

    grupoService.quitarAlumno(grupoId, alumnoId);

    return "redirect:/profesor/grupos/" + grupoId;
}

@PostMapping("/profesor/grupo/eliminar/{id}")
public String eliminarGrupo(@PathVariable Long id){
    grupoRepository.deleteById(id);
    return "redirect:/profesor/grupos";
}

    

    // ------------------------
    // EJERCICIOS
    // ------------------------

    @GetMapping("/profesor/ejercicios")
    public String verEjercicios(Model model){

        model.addAttribute("ejercicios", ejercicioService.listarTodos());

        return "profesor_ejercicios";
    }

    @GetMapping("/profesor/ejercicios/crear")
    public String crearEjercicioForm(Model model){
        model.addAttribute("ejercicio", new EjercicioRequestDTO());
        return "crear_ejercicio";
    }

 @PostMapping("/profesor/ejercicios/crear")
public String crearEjercicioWeb(@ModelAttribute EjercicioRequestDTO dto, Model model, Authentication authentication){

    try{

        String email = authentication.getName();

        Usuario profesor = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        Ejercicio ejercicio = new Ejercicio();

        ejercicio.setTitulo(dto.getTitulo());
        ejercicio.setDescripcion(dto.getDescripcion());
        ejercicio.setFechaLimite(dto.getFechaLimite());
        ejercicio.setFechaPublicacion(LocalDateTime.now());
        ejercicio.setCreador(profesor);

        ejercicioService.crearEjercicio(dto.getTitulo(), dto.getDescripcion(), dto.getFechaLimite(), profesor.getId());

        return "redirect:/profesor/ejercicios";

    }catch(RuntimeException e){

        model.addAttribute("error", e.getMessage());
        model.addAttribute("ejercicio", dto);

        return "crear_ejercicio";
    }
}

    @GetMapping("/profesor/ejercicios/{id}")
    public String verEjercicio(@PathVariable Long id, Model model){

        try{

            model.addAttribute("ejercicio", ejercicioService.obtenerPorId(id));
            model.addAttribute("preguntas", preguntaService.listarPorEjercicio(id));

            return "ver_ejercicio";

        }catch(RuntimeException e){

            return "redirect:/profesor/ejercicios";
        }
    }

    @GetMapping("/profesor/ejercicios/{id}/asignar")
    public String asignarEjercicioForm(@PathVariable Long id, Model model){
        Ejercicio ejercicio = ejercicioService.obtenerPorId(id);
        model.addAttribute("ejercicio", ejercicio);
        model.addAttribute("grupos", grupoService.listarTodos());
        // IDs de grupos ya asignados para pre-marcar los checkboxes
        Set<Long> gruposAsignados = ejercicio.getGrupos().stream()
                .map(Grupo::getId)
                .collect(java.util.stream.Collectors.toSet());
        model.addAttribute("gruposAsignados", gruposAsignados);
        return "asignar_ejercicio";
    }


    @PostMapping("/profesor/ejercicios/{id}/asignar")
    public String asignarEjercicio(@PathVariable Long id,
                                   @RequestParam(required = false) List<Long> gruposIds){
        // Si ningún checkbox marcado, gruposIds es null → pasar lista vacía para desasignar todos
        List<Long> ids = gruposIds != null ? gruposIds : new ArrayList<>();
        ejercicioService.asignarAGrupos(id, ids);
        return "redirect:/profesor/ejercicios";
    }

@PostMapping("/profesor/ejercicios/{id}/editar")
public String editarEjercicio(@PathVariable Long id,
                               @RequestParam String titulo,
                               @RequestParam(required = false) String descripcion,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaPublicacion,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaLimite) {
    ejercicioService.editarEjercicio(id, titulo, descripcion, fechaPublicacion, fechaLimite);
    return "redirect:/profesor/ejercicios/" + id + "?editado=true";
}

@PostMapping("/profesor/grupos/{id}/editar")
public String editarGrupo(@PathVariable Long id,
                           @RequestParam String nombre,
                           @RequestParam(required = false) String descripcion) {
    grupoService.editarGrupo(id, nombre, descripcion != null ? descripcion : "");
    return "redirect:/profesor/grupos/" + id + "?editado=true";
}

@PostMapping("profesor/ejercicio/eliminar/{id}")
@Transactional
public String eliminarEjercicio(@PathVariable Long id){

    Ejercicio ejercicio = ejercicioRepository.findById(id).orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));

    // Desasignar de grupos
    for(Grupo grupo : new ArrayList<>(ejercicio.getGrupos())){
        grupo.getEjercicios().remove(ejercicio);
        grupoRepository.save(grupo);
    }
    ejercicio.getGrupos().clear();

    // Borrar entregas antes de eliminar el ejercicio para evitar FK violation.
    // Entrega → (cascade) → Respuesta → (cascade) → Reclamacion
    List<Entrega> entregas = entregaRepository.findByEjercicio(ejercicio);
    entregaRepository.deleteAll(entregas);

    ejercicioRepository.delete(ejercicio);
    return "redirect:/profesor/ejercicios";
}

    // ------------------------
    // PREGUNTAS
    // ------------------------

    @GetMapping("/profesor/ejercicios/{id}/preguntas")
    public String verPreguntas(@PathVariable Long id, Model model){

        try{

            model.addAttribute("ejercicio", ejercicioService.obtenerPorId(id));
            model.addAttribute("preguntas", preguntaService.listarPorEjercicio(id));

            return "preguntas_ejercicio";

        }catch(RuntimeException e){

            return "redirect:/profesor/ejercicios";
        }
    }

    @GetMapping("/profesor/ejercicios/{id}/preguntas/crear")
    public String crearPreguntaForm(@PathVariable Long id, Model model){

        model.addAttribute("ejercicioId", id);

        return "crear_pregunta";
    }


    @PostMapping("/profesor/ejercicios/{id}/preguntas/crear")
public String crearPreguntaWeb(@PathVariable Long id,
                               @RequestParam String enunciado,
                               @RequestParam String tipo,
                               @RequestParam BigDecimal puntuacionMaxima,
                               @RequestParam(required = false) BigDecimal valorCorrecto,
                               @RequestParam(required = false) BigDecimal tolerancia,
                               @RequestParam(required = false) String rubrica,
                            HttpServletRequest request){



        Pregunta pregunta = new Pregunta();

        pregunta.setEnunciado(enunciado);
        pregunta.setTipo(Pregunta.TipoPregunta.valueOf(tipo));
        pregunta.setPuntuacionMaxima(puntuacionMaxima);


        if(Pregunta.TipoPregunta.valueOf(tipo) == TipoPregunta.TABLA){

            
            int filas = Integer.parseInt(request.getParameter("filas"));
            int columnas = Integer.parseInt(request.getParameter("columnas"));

            double toleranciaTabla = 0;

            String tol = request.getParameter("toleranciaTabla");

            if(tol != null && !tol.isEmpty()){
                toleranciaTabla = Double.parseDouble(tol);
            }

            double[][] respuestas = new double[filas][columnas];
            for (int i=0; i<filas; i++){
                for(int j=0; j<columnas; j++){

                    String valor = request.getParameter("celda_"+i+"_"+j);
                    if(valor == null || valor.isEmpty()){
                        respuestas[i][j] = 0;
                    } else{
                        respuestas[i][j] = Double.parseDouble(valor);
                    }
                    
                }
            }
            String[] cabecerasColumnas = new String[columnas];
            String[] cabecerasFilas = new String[filas];


            for(int j = 0; j<columnas; j++){
                cabecerasColumnas[j] = request.getParameter("col_"+j);
            }

            for(int i = 0; i<filas; i++){
                cabecerasFilas[i] = request.getParameter("fila_"+i);
            }

            
            Map<String, Object> config = new HashMap<>();

            config.put("filas", filas);
            config.put("columnas", columnas);
            config.put("respuestasCorrectas", respuestas);
            config.put("cabecerasColumnas", cabecerasColumnas);
            config.put("cabecerasFilas", cabecerasFilas);
            config.put("tolerancia", toleranciaTabla);

            try{

            ObjectMapper mapper = new ObjectMapper();

            pregunta.setConfiguracionJson(mapper.writeValueAsString(config));
            } catch(Exception e){
                throw new RuntimeException(e);
            }
        }







        if(Pregunta.TipoPregunta.valueOf(tipo) == TipoPregunta.NUMERICO){
            pregunta.setValorCorrecto(valorCorrecto);
            pregunta.setTolerancia(tolerancia);
        }

        // Rúbrica de corrección (solo para TEXTO e IMAGEN)
        if (rubrica != null && !rubrica.isBlank()) {
            pregunta.setRubrica(rubrica.trim());
        }

        preguntaService.crearPregunta(id, pregunta);


        return "redirect:/profesor/ejercicios/" + id + "/preguntas";
    }
@PostMapping("profesor/pregunta/eliminar/{id}")
@Transactional
public String eliminarPregunta(@PathVariable Long id){
    Pregunta pregunta = preguntaRepository.findById(id).orElseThrow(() -> new RuntimeException("Pregunta no encontrada"));
    Long ejercicioId = pregunta.getEjercicio().getId();
    // Cascade: Pregunta → Respuesta → Reclamacion (via @OneToMany cascade en Respuesta)
    preguntaRepository.delete(pregunta);
    return "redirect:/profesor/ejercicios/" + ejercicioId;
}

    

    // ------------------------
    // ENTREGAS PROFESOR
    // ------------------------

    @GetMapping("/profesor/entregas")
    public String verEntregasProfesor(Model model){

        List<Entrega> entregas = entregaService.listarTodas();
        model.addAttribute("entregas", entregas);

        return "profesor_entregas";
    }

    @PostMapping("/profesor/publicar_nota/{id}")
    public String publicarNota(@PathVariable Long id) {
        entregaService.publicarNota(id);
        return "redirect:/profesor/entregas";
    }

    @PostMapping("/profesor/grupos/{id}/publicar_notas")
    public String publicarNotasGrupo(@PathVariable Long id) {
        int publicadas = entregaService.publicarNotasPorGrupo(id);
        return "redirect:/profesor/grupos/" + id + "?publicadas=" + publicadas;
    }


    // ------------------------
    // PANEL ALUMNO
    // ------------------------

    @GetMapping("/alumno")
    public String panelAlumno(){

        return "alumno";
    }

    // ------------------------
    // EJERCICIOS ALUMNO
    // ------------------------

   @GetMapping("/alumno/ejercicios")
public String verEjerciciosAlumno(Model model, Authentication authentication) {

    String email = authentication.getName();

    Usuario alumno = usuarioRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

    Set<Grupo> grupos = alumno.getGruposComoAlumno();

    Set<Ejercicio> ejercicios = new HashSet<>();

    for (Grupo grupo : grupos) {
        ejercicios.addAll(grupo.getEjercicios());
    }

    // ejercicios ya entregados
    Set<Long> ejerciciosEntregados = new HashSet<>();

    List<Entrega> entregas = entregaService.listarPorAlumno(alumno.getId());

    for (Entrega entrega : entregas) {
        ejerciciosEntregados.add(entrega.getEjercicio().getId());
    }

    model.addAttribute("ejercicios", ejercicios);
    model.addAttribute("ejerciciosEntregados", ejerciciosEntregados);

    return "alumno_ejercicios";
}

    @GetMapping("/alumno/ejercicio/{id}")
    public String resolverEjercicio(@PathVariable Long id, Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario alumno = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
        Ejercicio ejercicio = ejercicioService.obtenerPorId(id);

        // Bloquear si ya entregado
        Optional<Entrega> entregaExistente = entregaRepository.findByAlumnoAndEjercicio(alumno, ejercicio);
        if (entregaExistente.isPresent() && entregaExistente.get().getEstado() != Entrega.EstadoEntrega.EN_PROGRESO) {
            return "ejercicio_enviado";
        }

        // Bloquear si fecha límite superada
        if (ejercicio.getFechaLimite() != null && java.time.LocalDateTime.now().isAfter(ejercicio.getFechaLimite())) {
            model.addAttribute("ejercicio", ejercicio);
            model.addAttribute("error", "El plazo de entrega ha finalizado.");
            return "resolver_ejercicio";
        }

        model.addAttribute("ejercicio", ejercicio);
        model.addAttribute("preguntas", preguntaService.listarPorEjercicio(id));
        return "resolver_ejercicio";
    }

    @PostMapping("/alumno/ejercicio/enviar")
    @Transactional
    public String enviarEjercicio(@RequestParam Long ejercicioId,
                                  @RequestParam Map<String, String> respuestas,
                                  HttpServletRequest request,
                                  Authentication authentication) {

        String email = authentication.getName();
        Usuario alumno = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

        // Verificar fecha límite también en el envío
        Ejercicio ejercicioCheck = ejercicioService.obtenerPorId(ejercicioId);
        if (ejercicioCheck.getFechaLimite() != null && java.time.LocalDateTime.now().isAfter(ejercicioCheck.getFechaLimite())) {
            return "redirect:/alumno/ejercicios";
        }

        // Evitar violación de restricción UNIQUE si ya existe una entrega
        Optional<Entrega> entregaExistente = entregaRepository.findByAlumnoAndEjercicio(alumno, ejercicioCheck);
        if (entregaExistente.isPresent() && entregaExistente.get().getEstado() != Entrega.EstadoEntrega.EN_PROGRESO) {
            // Ya fue entregada y procesada: redirigir sin crear duplicado
            return "redirect:/alumno/entregas";
        }
        Entrega entrega;
        if (entregaExistente.isPresent()) {
            // Entrega EN_PROGRESO de intento anterior: reutilizarla y borrar respuestas previas
            entrega = entregaExistente.get();
            respuestaRepository.deleteAll(respuestaRepository.findByEntregaId(entrega.getId()));
        } else {
            entrega = entregaService.crearEntrega(alumno.getId(), ejercicioId);
        }

        for (Pregunta pregunta : preguntaService.listarPorEjercicio(ejercicioId)) {

            if (pregunta.getTipo() == Pregunta.TipoPregunta.NUMERICO) {
                String key = "respuesta_" + pregunta.getId();
                String valor = respuestas.get(key);
                if (valor != null && !valor.isBlank()) {
                    Respuesta respuesta = new Respuesta();
                    respuesta.setRespuestaTexto(valor);
                    respuestaService.crearRespuesta(entrega.getId(), pregunta.getId(), respuesta);
                }
            }

            if (pregunta.getTipo() == Pregunta.TipoPregunta.TABLA) {
                BigDecimal nota = respuestaService.corregirTabla(pregunta, respuestas);
                String valoresJson = null;
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode config = mapper.readTree(pregunta.getConfiguracionJson());
                    int filas = config.get("filas").asInt();
                    int columnas = config.get("columnas").asInt();
                    double[][] valores = new double[filas][columnas];
                    for (int i = 0; i < filas; i++) {
                        for (int j = 0; j < columnas; j++) {
                            String key = "respuesta_" + pregunta.getId() + "_" + i + "_" + j;
                            String val = respuestas.getOrDefault(key, "");
                            valores[i][j] = (val == null || val.isBlank()) ? 0 : Double.parseDouble(val);
                        }
                    }
                    valoresJson = mapper.writeValueAsString(valores);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Respuesta respuesta = new Respuesta();
                respuesta.setRespuestaTexto("TABLA");
                respuesta.setRespuestaJson(valoresJson);
                respuesta.setPuntuacionObtenida(nota);
                respuestaService.crearRespuesta(entrega.getId(), pregunta.getId(), respuesta);
            }

            if (pregunta.getTipo() == Pregunta.TipoPregunta.TEXTO) {
                String key = "respuesta_" + pregunta.getId();
                String textoRespuesta = respuestas.getOrDefault(key, "").trim();
                if (!textoRespuesta.isEmpty()) {
                    // Incluir rúbrica del profesor en el enunciado para mejorar corrección IA
                    String enunciadoConRubrica = buildEnunciadoConRubrica(pregunta);
                    AiGradingResult resultado = aiGradingService.gradeText(
                            textoRespuesta, enunciadoConRubrica, pregunta.getPuntuacionMaxima());
                    Respuesta respuesta = new Respuesta();
                    respuesta.setRespuestaTexto(textoRespuesta);
                    respuesta.setFeedbackIa(resultado.getFeedback());
                    respuesta.setPuntuacionObtenida(resultado.getScore());
                    respuestaService.crearRespuesta(entrega.getId(), pregunta.getId(), respuesta);
                }
            }

            if (pregunta.getTipo() == Pregunta.TipoPregunta.IMAGEN) {
                if (request instanceof org.springframework.web.multipart.MultipartHttpServletRequest multipartRequest) {
                    MultipartFile archivo = multipartRequest.getFile("imagen_" + pregunta.getId());
                    if (archivo != null && !archivo.isEmpty()) {
                        String filename = storageService.store(archivo);
                        String absoluteImagePath = java.nio.file.Paths.get(uploadPath)
                                .toAbsolutePath().normalize().resolve(filename).toString();

                        String enunciadoConRubrica = buildEnunciadoConRubrica(pregunta);
                        AiGradingResult resultado = aiGradingService.gradeImage(
                                absoluteImagePath, enunciadoConRubrica, pregunta.getPuntuacionMaxima());

                        Respuesta respuesta = new Respuesta();
                        respuesta.setImagenUrl(filename);
                        respuesta.setFeedbackIa(resultado.getFeedback());
                        respuesta.setPuntuacionObtenida(resultado.getScore());
                        respuestaService.crearRespuesta(entrega.getId(), pregunta.getId(), respuesta);
                    }
                }
            }
        }

        entregaService.calcularNotaEntrega(entrega.getId());
        return "redirect:/alumno/entregas";
    }

    // ------------------------
    // ENTREGAS ALUMNO
    // ------------------------

    @GetMapping("/alumno/entregas")
    public String verEntregasAlumno(Model model, Authentication authentication){

        String email = authentication.getName();

        Usuario alumno = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

        List<Entrega> entregas = entregaService.listarPorAlumno(alumno.getId());

        model.addAttribute("entregas",entregas);
        return "alumno_entregas";
    }


    @GetMapping("/alumno/entrega/{id}")
    public String verEntregaAlumno(@PathVariable Long id, Model model, Authentication authentication) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrega no encontrada"));

        String email = authentication.getName();
        if (!entrega.getAlumno().getEmail().equals(email)) {
            return "redirect:/alumno/entregas";
        }

        if (!entrega.getVisibleAlumno()) {
            return "redirect:/alumno/entregas";
        }

        List<Respuesta> respuestas = respuestaRepository.findByEntregaId(id);

        Map<Long, Reclamacion> reclamacionesPorRespuesta = new HashMap<>();
        for (Respuesta r : respuestas) {
            reclamacionRepository.findByRespuestaId(r.getId())
                    .ifPresent(rec -> reclamacionesPorRespuesta.put(r.getId(), rec));
        }

        model.addAttribute("entrega", entrega);
        model.addAttribute("respuestas", respuestas);
        model.addAttribute("reclamacionesPorRespuesta", reclamacionesPorRespuesta);

        return "alumno_entrega";
    }



    @GetMapping("/profesor/entrega/{id}")
    public String verEntregaProfesor(@PathVariable Long id, Model model) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrega no encontrada"));

        List<Respuesta> respuestas = respuestaRepository.findByEntregaId(id);

        // Map respuestaId -> reclamacion (si existe)
        Map<Long, Reclamacion> reclamacionesPorRespuesta = new HashMap<>();
        for (Respuesta r : respuestas) {
            reclamacionRepository.findByRespuestaId(r.getId())
                    .ifPresent(rec -> reclamacionesPorRespuesta.put(r.getId(), rec));
        }

        model.addAttribute("entrega", entrega);
        model.addAttribute("respuestas", respuestas);
        model.addAttribute("reclamacionesPorRespuesta", reclamacionesPorRespuesta);

        return "profesor_entrega";
    }

    // ------------------------
    // RECLAMACIONES ALUMNO
    // ------------------------

    @PostMapping("/alumno/entrega/{entregaId}/reclamar/{respuestaId}")
    public String reclamar(@PathVariable Long entregaId,
                           @PathVariable Long respuestaId,
                           @RequestParam String comentario,
                           Authentication authentication) {
        try {
            reclamacionService.crearReclamacion(entregaId, respuestaId, comentario);
        } catch (RuntimeException e) {
            // reclamación ya existente u otro error — volvemos igualmente
        }
        return "redirect:/alumno/entrega/" + entregaId;
    }

    // ------------------------
    // RECLAMACIONES PROFESOR
    // ------------------------

    @GetMapping("/profesor/reclamaciones")
    public String verReclamaciones(Model model) {
        model.addAttribute("reclamaciones", reclamacionService.obtenerPendientes());
        return "profesor_reclamaciones";
    }

    @PostMapping("/profesor/reclamacion/{id}/resolver")
    public String resolverReclamacion(@PathVariable Long id,
                                      @RequestParam Long entregaId,
                                      @RequestParam BigDecimal nuevaNota,
                                      @RequestParam(required = false) String comentarioProfesor) {
        reclamacionService.resolverReclamacion(id, nuevaNota, comentarioProfesor);
        return "redirect:/profesor/entrega/" + entregaId;
    }

    // ------------------------
    // EXPORTAR NOTAS
    // ------------------------

    @GetMapping("/profesor/grupos/{id}/exportar-notas")
    public ResponseEntity<byte[]> exportarNotas(@PathVariable Long id) {
        Grupo grupo = grupoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        List<Entrega> todasEntregas = entregaRepository.findByEjercicio_Grupos_Id(id);

        // Build a map: alumnoId → list of entregas
        Map<Long, List<Entrega>> entregasPorAlumno = new HashMap<>();
        for (Entrega e : todasEntregas) {
            entregasPorAlumno
                    .computeIfAbsent(e.getAlumno().getId(), k -> new ArrayList<>())
                    .add(e);
        }

        // CSV with semicolons (Excel-compatible in European locale)
        // Opening with Excel: File → Open → CSV → use ";" as separator
        // Or just double-click the file if Windows locale uses semicolons
        StringBuilder sb = new StringBuilder();
        sb.append("sep=;\n"); // hint for Excel on Windows
        sb.append("Nombre;Email;Ejercicio;Nota;Nota maxima;Estado\n");

        for (Usuario alumno : grupo.getAlumnos()) {
            List<Entrega> entregasAlumno = entregasPorAlumno.getOrDefault(alumno.getId(), List.of());
            if (entregasAlumno.isEmpty()) {
                // One row with no grades to show the student exists
                sb.append(csvCell(alumno.getNombre())).append(";")
                  .append(csvCell(alumno.getEmail())).append(";")
                  .append("Sin entrega;;;;\n");
            } else {
                for (Entrega e : entregasAlumno) {
                    BigDecimal nota = e.getNotaTotal() != null ? e.getNotaTotal() : BigDecimal.ZERO;
                    // Max score = sum of all questions' puntuacionMaxima
                    BigDecimal maxNota = e.getEjercicio().getPreguntas().stream()
                            .map(p -> p.getPuntuacionMaxima() != null ? p.getPuntuacionMaxima() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    sb.append(csvCell(alumno.getNombre())).append(";")
                      .append(csvCell(alumno.getEmail())).append(";")
                      .append(csvCell(e.getEjercicio().getTitulo())).append(";")
                      .append(nota.toPlainString().replace('.', ',')).append(";")  // comma decimal for Excel ES
                      .append(maxNota.toPlainString().replace('.', ',')).append(";")
                      .append(e.getEstado()).append("\n");
                }
            }
        }

        byte[] bytes = ("﻿" + sb).getBytes(StandardCharsets.UTF_8); // BOM for Excel UTF-8
        String filename = "notas_" + grupo.getNombre().replaceAll("[^a-zA-Z0-9_-]", "_") + ".csv";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    /** Wraps a CSV cell value in quotes and escapes internal quotes. */
    private String csvCell(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    /**
     * Construye el enunciado que se enviará a la IA, incluyendo la rúbrica
     * del profesor si está definida. Esto mejora la precisión de corrección
     * equivaliendo a proporcionarle ejemplos de respuesta correcta (few-shot).
     */
    private String buildEnunciadoConRubrica(Pregunta pregunta) {
        String enunciado = pregunta.getEnunciado();
        String rubrica = pregunta.getRubrica();
        if (rubrica != null && !rubrica.isBlank()) {
            return enunciado + "\n\n--- Rúbrica de corrección ---\n" + rubrica.trim();
        }
        return enunciado;
    }

    // ------------------------
    // RECUPERACIÓN CONTRASEÑA
    // ------------------------

    @GetMapping("/recuperar-contrasena")
    public String recuperarContrasenaForm() {
        return "recuperar_contrasena";
    }

    @PostMapping("/recuperar-contrasena")
    public String recuperarContrasena(@RequestParam String email, Model model) {
        // Always show the same message to prevent email enumeration
        String mensaje = "Si tu correo está registrado recibirás un enlace de recuperación en breve.";
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email.toLowerCase().trim());
            if (usuarioOpt.isPresent()) {
                String token = UUID.randomUUID().toString();
                TokenRecuperacion tr = new TokenRecuperacion();
                tr.setUsuario(usuarioOpt.get());
                tr.setToken(token);
                tr.setExpiracion(LocalDateTime.now().plusHours(1));
                tr.setUsado(false);
                tokenRecuperacionRepository.save(tr);
                emailService.enviarEmailRecuperacion(email.toLowerCase().trim(), token);
            }
        } catch (Exception e) {
            System.err.println("[WebController] Error en recuperación: " + e.getMessage());
        }
        model.addAttribute("mensaje", mensaje);
        return "recuperar_contrasena";
    }

    @GetMapping("/reset-contrasena")
    public String resetContrasenaForm(@RequestParam String token, Model model) {
        Optional<TokenRecuperacion> trOpt = tokenRecuperacionRepository.findByToken(token);
        if (trOpt.isEmpty() || trOpt.get().getUsado()
                || trOpt.get().getExpiracion().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "El enlace de recuperación no es válido o ha expirado.");
            return "reset_contrasena";
        }
        model.addAttribute("token", token);
        return "reset_contrasena";
    }

    @PostMapping("/reset-contrasena")
    public String resetContrasena(@RequestParam String token,
                                  @RequestParam String nuevaPassword,
                                  Model model) {
        Optional<TokenRecuperacion> trOpt = tokenRecuperacionRepository.findByToken(token);
        if (trOpt.isEmpty() || trOpt.get().getUsado()
                || trOpt.get().getExpiracion().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "El enlace de recuperación no es válido o ha expirado.");
            return "reset_contrasena";
        }
        try {
            TokenRecuperacion tr = trOpt.get();
            usuarioService.cambiarPassword(tr.getUsuario().getId(), nuevaPassword);
            tr.setUsado(true);
            tokenRecuperacionRepository.save(tr);
            return "redirect:/login?passwordReset=true";
        } catch (RuntimeException e) {
            model.addAttribute("token", token);
            model.addAttribute("error", e.getMessage());
            return "reset_contrasena";
        }
    }
}
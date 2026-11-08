package com.taller.asistente.dto;

import java.util.List;

public record ConsultaResponse(RespuestaIA analisis, List<ResultadoSimilitud> registros) {}

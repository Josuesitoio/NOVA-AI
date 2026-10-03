package mx.edu.itson.proyectonova.services.interfaces;

import mx.edu.itson.proyectonova.models.dtos.DatosFisicosResponse;

public interface ExploracionService {
    DatosFisicosResponse analizarAstro(String kepid);
}
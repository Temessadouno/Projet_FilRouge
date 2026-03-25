package com.commerce.service;

import com.commerce.model.Categorie;
import com.commerce.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorieService {
    private final CategorieRepository categorieRepository;
    public List<Categorie> listerToutes() { return categorieRepository.findAll(); }
    public Categorie sauvegarder(Categorie c) { return categorieRepository.save(c); }
    public Categorie findById(Integer id) { return categorieRepository.findById(id).orElseThrow(); }
    public void supprimer(Integer id) { categorieRepository.deleteById(id); }
}
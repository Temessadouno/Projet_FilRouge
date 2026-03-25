package com.commerce.service;

import com.commerce.model.Produit;
import com.commerce.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;

    public List<Produit> listerActifs() { return produitRepository.findByDeletedFalse(); }
    public List<Produit> listerTous()   { return produitRepository.findAll(); }
    public Produit findById(Integer id) {
        return produitRepository.findById(id).orElseThrow(() -> new RuntimeException("Produit introuvable"));
    }
    public Produit sauvegarder(Produit p)  { return produitRepository.save(p); }
    public List<Produit> rechercher(String q) { return produitRepository.search(q); }
    public List<Produit> parCategorie(Integer catId) {
        return produitRepository.findByCategorieIdAndDeletedFalse(catId);
    }

    @Transactional
    public void supprimerLogique(Integer id) {
        Produit p = findById(id);
        p.setDeleted(true);
        produitRepository.save(p);
    }

    @Transactional
    public void ajusterStock(Integer produitId, int delta) {
        Produit p = findById(produitId);
        int nouveauStock = p.getStock() + delta;
        if (nouveauStock < 0) throw new RuntimeException("Stock insuffisant pour: " + p.getNom());
        p.setStock(nouveauStock);
        produitRepository.save(p);
    }
}
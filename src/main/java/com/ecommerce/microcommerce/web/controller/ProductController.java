package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Api(description = "API pour es opérations CRUD sur les produits.")

@RestController
public class ProductController {

	@Autowired
	private ProductDao productDao;

	// Récupérer la liste des produits

	@RequestMapping(value = "/Produits", method = RequestMethod.GET)

	public MappingJacksonValue listeProduits() {

		Iterable<Product> produits = productDao.findAll();

		SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

		FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

		MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

		produitsFiltres.setFilters(listDeNosFiltres);

		return produitsFiltres;
	}

	// Récupérer un produit par son Id
	@ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
	@GetMapping(value = "/Produits/{id}")

	public Product afficherUnProduit(@PathVariable int id) {

		Product produit = productDao.findById(id);

		if (produit == null)
			throw new ProduitIntrouvableException(
					"Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");

		return produit;
	}

	// ajouter un produit
	@ApiOperation(value = "Methode d'ajout de produit")
	@PostMapping(value = "/Produits")

	public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {
		System.out.println("Le prix est : "+product.getPrix());
		if ((product.getPrix() == 0))
			
			throw new ProduitGratuitException("Prix invalide, merci de fournir un prix valide");
		
		Product productAdded = productDao.save(product);

		if (productAdded == null)
			return ResponseEntity.noContent().build();

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(productAdded.getId()).toUri();

		return ResponseEntity.created(location).build();
	}

	@DeleteMapping(value = "/Produits/{id}")
	public void supprimerProduit(@PathVariable int id) {

		productDao.delete(id);
	}
	
	
	@PutMapping(value = "/Produits")
	public void updateProduit(@RequestBody Product product) {

		productDao.save(product);
	}

	// Pour les tests
	@GetMapping(value = "test/produits/{prix}")
	public List<Product> testeDeRequetes(@PathVariable int prix) {

		return productDao.chercherUnProduitCher(400);
	}

	@ApiOperation(value = "Affichage des marges des produits")
	@GetMapping(value = "/AdminProduits")
	public List<String> testList() {
		List<String> liste = new ArrayList<>();
		List<Product> listes = new ArrayList<>();
		listes = productDao.findAll();

		for (int i = 0; i < listes.size(); i++) {
			liste.add(listes.get(i).toString() + ":" + (listes.get(i).getPrix() - listes.get(i).getPrixAchat()));
		}
		return liste;
	}

	@ApiOperation(value = "Methode qui retourne les produits par ordre alphabetique")
	@GetMapping(value = "Produits/Tri")
	public List<Product> trierProduitsParOrdreAlphabetique() {
		return productDao.listeProduitTrierParOrdreAlbhabetique();
	}

}

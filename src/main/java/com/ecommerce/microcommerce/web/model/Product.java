package com.ecommerce.microcommerce.web.model;
import com.fasterxml.jackson.annotation.JsonFilter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

///Elle demande à ce que prixAchat et id ne soient pas renvoyés en cas de requête pour récupérer l'entité/le Bean correspondant.
//@JsonIgnoreProperties(value = "prixAchat", "id")
//@JsonFilter("monFiltreDynamique")
@Entity
//@Table(name = "product")
public class Product {
    //Generate(strategy = GenerationType.IDENTITY)
    @Id
    //@GeneratedValue
    private int id;
    @Size(min = 3, max = 25)
    private String nom;
    @Min(value = 1)
    private int prix;

    //information que nous ne souhaitons pas exposer
    private int prixAchat;

    public Product() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getPrix() {
        return prix;
    }

    public void setPrix(int prix) {
        this.prix = prix;

    }

    public Product(int id, String nom, int prix, int prixAchat) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.prixAchat = prixAchat;
    }

    public Product(int id, String nom, int prix) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
    }

    public int getPrixAchat() {
        return prixAchat;
    }

    public void setPrixAchat(int prixAchat) {
        this.prixAchat = prixAchat;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                '}';
    }
}

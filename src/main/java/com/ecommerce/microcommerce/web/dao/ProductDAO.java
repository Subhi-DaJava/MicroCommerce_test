package com.ecommerce.microcommerce.web.dao;

import com.ecommerce.microcommerce.web.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDAO extends JpaRepository<Product,Integer> {
    //Notre méthode
    /*
    Les conventions ! Spring Data JPA propose un ensemble de conventions qui lui permettront de
    déduire la bonne requête à partir du nom de la méthode.
     */
    Product findById(int id);
    /*
    Prenons comme exemple la récupération des produits dont le prix est supérieur à 400.
    Commençons par ajouter une méthode findByPrixGreaterThan au Repository, à l'intérieur de la définition de l'interface ProductDAO :
     */
    List<Product> findByPrixGreaterThan(int prixLimit);

   /* @Query("SELECT id, nom, prix FROM Product p WHERE p.prix > :prixLimit")
    List<Product>  chercherUnProduitCher(@Param("prixLimit") int prix);*/

    //SELECT id, nom, prix FROM Product p WHERE p.prix > ?1

}

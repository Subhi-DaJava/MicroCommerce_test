package com.ecommerce.microcommerce.web.controller;


import com.ecommerce.microcommerce.web.dao.ProductDAO;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.ecommerce.microcommerce.web.model.Product;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
//ajouter une description pour chaque API grâce à l'annotation @Api
@Api("API pour les opération CRUD sur les produits !")
@RestController
public class ProductController {
    @Autowired
    private ProductDAO productDAO;
    //Même pas besoin de spécifier son implémentation(ProductDAO -> ProductDAOImpl)
    /**
     * Tout d'abord, nous avons créé une variable de type ProductDao, que nous avons définie en private final afin que
     * Spring se charge d'en fabriquer une instance que nous injectons dans le constructeur. ProductDao a désormais accès à toutes les méthodes
     * que nous avons définies.
     */

    /**
     * SimpleBeanPropertyFilter est une implémentation de PropertyFilter qui permet d'établir les règles de filtrage sur un Bean donné.
     * La règle serializeAllExcept("") qui exclut uniquement les propriétés que nous souhaitons ignorer.
     * Inversement, vous pouvez procéder avec la méthode filterOutAllExcept qui marque toutes les propriétés comme étant à ignorer,
     * sauf celles passées en argument.
     * Maintenant que nous avons établi notre règle de filtrage, la ligne suivante nous permet d'indiquer à Jackson à quel Bean
     * l'appliquer. Nous utilisons SimpleFilterProvider pour déclarer que les règles de filtrage que nous avons créées (monFiltre)
     * peuvent s'appliquer à tous les Beans qui sont annotés avec monFiltreDynamique.
     * Jusqu'à présent, nous n'avons encore rien filtré concrètement ! Nous avons établi la règle et indiqué que cette règle
     * ne s'applique qu'aux Beans qui sont annotés avec monFiltreDynamique, mais nous ne l'avons pas encore appliquée.
     * Pour cela, nous les mettons au format MappingJacksonValue. Cela permet de donner accès aux méthodes qui nous intéressent,
     * comme setFilters qui applique les filtres que nous avons établis à la liste de Product.
     * Nous retournons ensuite la liste filtrée. Ne vous inquiétez pas si vous devez renvoyer MappingJacksonValue,
     * car ce n'est qu'un simple "conteneur" qui ne change absolument rien au contenu. MappingJacksonValue est donc exactement
     * comme produits, avec des méthodes de filtrage en plus.
     */
    //Récupérer la liste des produits par filtre
    //Nous savons appliquer des filtres sur notre API grâce à Jackson.
    @ApiOperation("Récupère tous les produit")
    @RequestMapping(value = "/Produits", method = RequestMethod.GET)
    public MappingJacksonValue listeProduits(){
        /*
        Parmi les opérations qu'offre JpaRepository, nous trouvons findAll() qui permet de récupérer toutes les données
        de l'entité concernée. findAll() retourne un Iterable, qu'il nous faut spécifier comme type de retour de la méthode.
        Dans notre cas, le type de retour est bien un Iterable, puisque nous retournons une réponse filtrée de type MappingJacksonValue.
        */
        Iterable<Product> produits = productDAO.findAll();
        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

        produitsFiltres.setFilters(listDeNosFiltres);
        return produitsFiltres;
    }
    //la même méthode, mais moins exigeante
   /* @ApiOperation("Afficher tous les produits!")
    @GetMapping("/Produits")
    public List<Product> getListeOfProduits(){
        return productDAO.findAll();
    }*/

    /**
     * URI "/Produits/{id}", renvoyer un produit au format JSON qui correspond à la classe Product.
     */
    //Récupérer un produit par son Id
    /*
    La magie de Spring Data va au-delà des requêtes CRUD. Le framework est capable de générer
    la requête SQL automatiquement en partant du nom de votre méthode !
    findById(id) est créée par nous même
     */
    @ApiOperation("Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/Produits/{id}")
    public Product afficherUnProduit(@PathVariable int id){
        Product product = productDAO.findById(id);
        if(product == null)
            throw new ProduitIntrouvableException("Le produit avec l'id "+id+" est INTROUVABLE. Écran bleu si je pouvais.");
        return product;
    }
    /*
    Les conventions ! Spring Data JPA propose un ensemble de conventions qui lui permettront de déduire
    la bonne requête à partir du nom de la méthode.
    Prenons comme exemple la récupération des produits dont le prix est supérieur à 400. Commençons par ajouter
    une méthode findByPrixGreaterThan au Repository, à l'intérieur de la définition de l'interface ProductDAO : List<Product> findByPrixGreaterThan(int prixLimit);
    findBy : indique que l'opération à exécuter est un SELECT.
    Prix : fournit le nom de la propriété sur laquelle le SELECT s'applique.
    GreaterThan : définit une condition "plus grand que".
     */
    @ApiOperation("Récupérer les ou le produit(s) en comparant le prix fourni avec tous les prix des produits!")
    @GetMapping(value = "test/Produits/{prixLimit}")
    public List<Product> testDeRequetes(@PathVariable int prixLimit){
        return productDAO.findByPrixGreaterThan(prixLimit);
    }


    /**
     *Dans un premier temps, nous faisons appel à la DAO pour ajouter le produit. Dans le cas où le produit ajouté est vide ou n'existe pas, nous retournons le code 204 No Content.
     *  Pour cela, la méthode noContent() est utilisée. Cette méthode est chaînée avec la méthode build() qui construit le header, et y ajoute le code choisi.
     * Dans le cas où tout s'est bien passé et que productAdded n'est donc pas null, nous avons besoin, en plus du code 201, d'ajouter l'URI vers cette nouvelle ressource créée,
     * afin d'être conformes au protocole HTTP.
     * Nous déclarons donc une instance de la classe URI afin de la passer ensuite comme argument de ResponseEntity. Nous instancions cette URI à partir de l'URL de la requête reçue.
     * Nous ajoutons ensuite l'Id du produit à l'URI à l'aide de la méthode buildAndExpand. Nous retrouvons l'Id dans l'instance de Product que nous avons reçu : productAdded.getId().
     * Enfin, nous invoquons la méthode created de ResponseEntity, qui accepte comme argument l'URI de la ressource nouvellement créée, et renvoie le code de statut 201.
     */
    //Nous avons remplacé les types de retour pour rendre notre application cohérente avec la norme, grâce à ResponseEntity.
    @ApiOperation("Ajouter un produit dans la BDD!")
    @PostMapping(value = "/Produits")
    public ResponseEntity<Product> ajouterProduit(@RequestBody Product product) {
        if(product.getPrix() == 0 ){
            throw new ProduitGratuitException("Le prix de vente n'est gratuit");
        }
        Product productAdded = productDAO.save(product);
        if (Objects.isNull(productAdded)) {
            return ResponseEntity.noContent().build();
        }
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }
    //Supprimer un produit
    @ApiOperation("Supprimer un produit grâce à son ID à condition que celui-ci soit en stock!")
    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {
        productDAO.deleteById(id);
    }
    //Mettre à jours un produit (le même id)
    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product)
    {
        productDAO.save(product);
    }

    //créer une méthode en utilisant des mots clés, afin que Spring Data JPA génère automatiquement la requête.
    @ApiOperation("Afficher tous les produits par order alphabétique!")
    @GetMapping("/TriProduits")
    public List<Product> trierProduitsParOrdreAlphabetique(){
        return productDAO.findAllByOrderByNom();
    }


    @GetMapping("/AdminProduits")
    public HashMap<Product,Integer> calculerMargeProduit(){
        HashMap<Product,Integer> map = new HashMap<>();

        List<Product> listProduits = productDAO.findAll();
        List<Integer> margePrix = new ArrayList<>();

        for (Product listProduit : listProduits) {
            margePrix.add(listProduit.getPrix() - listProduit.getPrixAchat());
        }
        for(int i = 0; i < listProduits.size(); i++){
            map.put(listProduits.get(i), margePrix.get(i));
        }
        //Le résulta ci-dessous, c'est généré automatique?
        /*"Product{id=3, nom='Table de Ping Pong', prix=750}": 350,
                "Product{id=1, nom='Ordinateur portable', prix=350}": 230,
                "Product{id=2, nom='Aspirateur Robot', prix=500}": 300*/

        /*map.entrySet().forEach(entry -> {
            System.out.println(entry.getKey()+":"+entry.getValue());
        });*/
        /*for (Product product : map.keySet()) {
        String key = product.toString();
        String value = map.get(product).toString();
        System.out.println(key + " : " + value);
        }*/
        return map;
    }
    @GetMapping("/ProduitsChers")
    public List<Product> chercherProduitchers(){
        return productDAO.chercherUnProduitCher(300);
    }


}

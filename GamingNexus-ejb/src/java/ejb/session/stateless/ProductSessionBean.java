/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Game;
import entity.Hardware;
import entity.OtherSoftware;
import entity.Product;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author yangxi
 */
@Stateless
public class ProductSessionBean implements ProductSessionBeanLocal {

    @EJB
    private OtherSoftwareSessionBeanLocal otherSoftwareSessionBean;

    @EJB
    private HardwareSessionBeanLocal hardwareSessionBean;

    @EJB
    private GameSessionBeanLocal gameSessionBean;

    @PersistenceContext(unitName = "GamingNexus-ejbPU")
    private EntityManager em;

    @Override
    public Product retrieveProductById(Long productId) {
        Product retrievedProduct = (Product) em.find(Product.class, productId);
        if (retrievedProduct instanceof Game) {
            Game retrievedGame = (Game) em.find(Game.class, productId);
            this.lazyLoadGame(retrievedGame);
            return retrievedGame;
        } else if (retrievedProduct instanceof Hardware) {
            Hardware retrievedHardware = (Hardware) em.find(Hardware.class, productId);
            this.lazyLoadHardware(retrievedHardware);
            return retrievedHardware;
        } else if (retrievedProduct instanceof OtherSoftware) {
            OtherSoftware retrivedOtherSoftware = (OtherSoftware) em.find(OtherSoftware.class, productId);
            this.lazyLoadOtherSoftware(retrivedOtherSoftware);
            return retrivedOtherSoftware;
        }
        assert false : "Product must always be a child entity";
        return null;
    }

//    @Override
//    public List<Tag> retrievedTagsByProduct(Long productID){
//     Product retrievedProduct = em.find(Product.class, productID);
//     retrievedProduct.getTags().size();
//     return retrievedProduct.get;
//             
//    }
    @Override
    public List<Product> searchProductsByName(String searchString) {
        Query query = em.createQuery("SELECT p FROM Product p WHERE p.name LIKE :inSearchString ORDER BY p.averageRating ASC");
        query.setParameter("inSearchString", "%" + searchString + "%");
        List<Product> productEntities = query.getResultList();

        for (Product product : productEntities) {
            if (product instanceof Game) {

                this.lazyLoadGame((Game)product);

            } else if (product instanceof Hardware) {

                this.lazyLoadHardware((Hardware)product);

            } else if (product instanceof OtherSoftware) {

                this.lazyLoadOtherSoftware((OtherSoftware)product);

            }
        }

        return productEntities;
    }

    public List<Product> filterProductsByTags(List<Long> tagIds, String condition) {
        List<Product> productEntities = new ArrayList<>();

        if (tagIds == null || tagIds.isEmpty() || (!condition.equals("AND") && !condition.equals("OR"))) {
            return productEntities;
        } else {
            if (condition.equals("OR")) {
                Query query = em.createQuery("SELECT DISTINCT pe FROM ProductEntity pe, IN (pe.tagEntities) te WHERE te.tagId IN :inTagIds ORDER BY pe.productId");
                query.setParameter("inTagIds", tagIds);
                productEntities = query.getResultList();
            } else // AND
            {
                String selectClause = "SELECT pe FROM ProductEntity pe";
                String whereClause = "";
                Boolean firstTag = true;
                Integer tagCount = 1;

                for (Long tagId : tagIds) {
                    selectClause += ", IN (pe.tagEntities) te" + tagCount;

                    if (firstTag) {
                        whereClause = "WHERE te1.tagId = " + tagId;
                        firstTag = false;
                    } else {
                        whereClause += " AND te" + tagCount + ".tagId = " + tagId;
                    }

                    tagCount++;
                }

                String jpql = selectClause + " " + whereClause + " ORDER BY pe.productId";
                Query query = em.createQuery(jpql);
                productEntities = query.getResultList();
            }

            for (Product productEntity : productEntities) {
                productEntity.getCategory();
                productEntity.getTags().size();
            }

            Collections.sort(productEntities, new Comparator<Product>() {
                public int compare(Product pe1, Product pe2) {
                    return pe1.getProductId().compareTo(pe2.getProductId());
                }
            });
            return productEntities;
        }
    }

    @Override
    public void deleteProduct(Product productToBeDeleted) {
        Game gameToBeDeleted;
        Hardware hardwareToBeDeleted;
        OtherSoftware otherSoftwareToBeDeleted;
        if (productToBeDeleted instanceof Game) {
            gameToBeDeleted = (Game) productToBeDeleted;
            em.remove(this.retrieveProductById(gameToBeDeleted.getProductId()));
        } else if (productToBeDeleted instanceof Hardware) {
            hardwareToBeDeleted = (Hardware) productToBeDeleted;
            em.remove(this.retrieveProductById(hardwareToBeDeleted.getProductId()));
        } else if (productToBeDeleted instanceof OtherSoftware) {
            otherSoftwareToBeDeleted = (OtherSoftware) productToBeDeleted;
            em.remove(this.retrieveProductById(otherSoftwareToBeDeleted.getProductId()));
        }
        em.flush();
    }

    public void persist(Object object) {
        em.persist(object);
    }

    public void lazyLoadGame(Game game) {
        game.getTags().size();
        game.getPromotions().size();
        game.getRatings().size();
        game.getOwnedItems().size();
        game.getPromotions().size();
        game.getRatings().size();
        game.getGameAccounts().size();
        game.getPictureURLs().size();
        game.getVideoURLs().size();
    }

    public void lazyLoadOtherSoftware(OtherSoftware otherSoftware) {
        otherSoftware.getTags().size();
        otherSoftware.getPromotions().size();
        otherSoftware.getRatings().size();
        otherSoftware.getOwnedItems().size();
        otherSoftware.getPromotions().size();
        otherSoftware.getRatings().size();
        otherSoftware.getPictureURLs().size();
        otherSoftware.getVideoURLs().size();
    }

    public void lazyLoadHardware(Hardware hardware) {
        hardware.getTags().size();
        hardware.getOwnedItems().size();
        hardware.getPromotions().size();
        hardware.getRatings().size();
        hardware.getDeliverables().size();
        hardware.getPictureURLs().size();
        hardware.getVideoURLs().size();
    }
}

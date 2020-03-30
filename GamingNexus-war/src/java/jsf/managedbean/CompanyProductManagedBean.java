/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf.managedbean;

import ejb.session.stateless.CategorySessionBeanLocal;
import ejb.session.stateless.CompanySessionBeanLocal;
import ejb.session.stateless.GameSessionBeanLocal;
import ejb.session.stateless.ProductSessionBeanLocal;
import ejb.session.stateless.TagSessionBeanLocal;
import entity.Category;
import entity.Company;
import entity.Game;
import entity.Hardware;
import entity.OtherSoftware;
import entity.Product;
import entity.Tag;
import java.io.IOException;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import util.exception.CompanyNotFoundException;
import util.exception.CreateNewProductException;
import util.exception.InputDataValidationException;
import util.exception.ProductSkuCodeExistException;
import util.exception.SystemAdminUsernameExistException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author root
 */
@Named(value = "companyProductManagedBean")
@ViewScoped
public class CompanyProductManagedBean implements Serializable {

    @EJB
    private ProductSessionBeanLocal productSessionBean;

    @EJB
    private GameSessionBeanLocal gameSessionBean;

    @EJB
    private CategorySessionBeanLocal categorySessionBean;

    @EJB
    private TagSessionBeanLocal tagSessionBean;

    @EJB
    private CompanySessionBeanLocal companySessionBeanLocal;
    @Inject
    private ViewProductManagedBean viewProductManagedBean;
    private Game newGame, gameToBeUpdated, gameToViewInDetails = null;
    private Product productToViewInDetails;
    private Hardware hardwareToViewInDetails = null;
    private OtherSoftware otherSoftwareToViewInDetails = null;
    private List<Product> products, filteredProducts;
    private List<Category> categories;
    private List<Tag> tags;
    private Company company;

    public CompanyProductManagedBean() {
        newGame = new Game();
        gameToBeUpdated = new Game();
    }

    @PostConstruct
    public void postConstruct() {
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        setCompany((Company) sessionMap.get("company"));
        System.out.println("company name: " + getCompany().getUsername());
        products = getCompany().getProducts();

        categories = categorySessionBean.retrieveAllCategories();
        tags = tagSessionBean.retrieveAllTags();
    }

    public void viewProductDetailsMethod(ActionEvent event) throws IOException {
          Long productIdToView = (Long)event.getComponent().getAttributes().get("productId");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("productIdToView", productIdToView);
     //   FacesContext.getCurrentInstance().getExternalContext().redirect("viewProductDetails.xhtml");
    }

    public void createNewSystemAdmin(ActionEvent event) throws SystemAdminUsernameExistException {

        String buttonID = event.getComponent().getId();

        switch (buttonID) {
            case "AddGameButton":
                try {
                    List<Long> tagIds = new ArrayList<>();
                    newGame.getTags().forEach(tag -> {
                        tagIds.add(tag.getTagId());
                    });
                    Game game = gameSessionBean.createNewGame(newGame, newGame.getCategory().getCategoryId(), tagIds, getCompany().getUserId());
                    products.add((Product) game);

                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "New Game " + newGame.getName() + " added successfully "
                                    + "(ID: " + game.getProductId() + ")", null));
                    newGame = new Game();

                } catch (UnknownPersistenceException | ProductSkuCodeExistException | InputDataValidationException | CreateNewProductException
                        | CompanyNotFoundException ex) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error has occurred while creating a new system admin  " + ex.getMessage(), null));
                }
                break;
            case "AddSoftwareButton":
                break;
            case "AddHardwareButton":
                break;
            default:
                break;

        }

    }

    public void deleteProduct(ActionEvent event) {
        try {
            Product productToBeDeleted = (Product) event.getComponent().getAttributes().get("productToBeDeleted");
            productSessionBean.deleteProduct(productToBeDeleted);
            products.remove(productToBeDeleted);

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Product deleted successfully ID: " + productToBeDeleted.getProductId(), null));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "An unexpected error has occurred: " + ex.getMessage(), null));
        }
    }

    /**
     * @return the products
     */
    public List<Product> getProducts() {
        return products;
    }

    /**
     * @param products the products to set
     */
    public void setProducts(List<Product> products) {
        this.products = products;
    }

    /**
     * @return the categories
     */
    public List<Category> getCategories() {
        return categories;
    }

    /**
     * @param categories the categories to set
     */
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    /**
     * @return the tags
     */
    public List<Tag> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    /**
     * @return the newGame
     */
    public Game getNewGame() {
        return newGame;
    }

    /**
     * @param newGame the newGame to set
     */
    public void setNewGame(Game newGame) {
        this.newGame = newGame;
    }

    /**
     * @return the gameToBeUpdated
     */
    public Game getGameToBeUpdated() {
        return gameToBeUpdated;
    }

    /**
     * @param gameToBeUpdated the gameToBeUpdated to set
     */
    public void setGameToBeUpdated(Game gameToBeUpdated) {
        this.gameToBeUpdated = gameToBeUpdated;
    }

    /**
     * @return the filteredProducts
     */
    public List<Product> getFilteredProducts() {
        return filteredProducts;
    }

    /**
     * @param filteredProducts the filteredProducts to set
     */
    public void setFilteredProducts(List<Product> filteredProducts) {
        this.filteredProducts = filteredProducts;
    }

    /**
     * @return the productToViewInDetails
     */
    public Product getProductToViewInDetails() {
        return productToViewInDetails;
    }

    /**
     * @param productToViewInDetails the productToViewInDetails to set
     */
    public void setProductToViewInDetails(Product productToViewInDetails) {
        this.productToViewInDetails = productToViewInDetails;
    }

    /**
     * @return the gameToViewInDetails
     */
    public Game getGameToViewInDetails() {
        return gameToViewInDetails;
    }

    /**
     * @param gameToViewInDetails the gameToViewInDetails to set
     */
    public void setGameToViewInDetails(Game gameToViewInDetails) {
        this.gameToViewInDetails = gameToViewInDetails;
    }

    /**
     * @return the hardwareToViewInDetails
     */
    public Hardware getHardwareToViewInDetails() {
        return hardwareToViewInDetails;
    }

    /**
     * @param hardwareToViewInDetails the hardwareToViewInDetails to set
     */
    public void setHardwareToViewInDetails(Hardware hardwareToViewInDetails) {
        this.hardwareToViewInDetails = hardwareToViewInDetails;
    }

    /**
     * @return the otherSoftwareToViewInDetails
     */
    public OtherSoftware getOtherSoftwareToViewInDetails() {
        return otherSoftwareToViewInDetails;
    }

    /**
     * @param otherSoftwareToViewInDetails the otherSoftwareToViewInDetails to
     * set
     */
    public void setOtherSoftwareToViewInDetails(OtherSoftware otherSoftwareToViewInDetails) {
        this.otherSoftwareToViewInDetails = otherSoftwareToViewInDetails;
    }

    /**
     * @return the company
     */
    public Company getCompany() {
        return company;
    }

    /**
     * @param company the company to set
     */
    public void setCompany(Company company) {
        this.company = company;
    }

    /**
     * @return the viewProductManagedBean
     */
    public ViewProductManagedBean getViewProductManagedBean() {
        return viewProductManagedBean;
    }

    /**
     * @param viewProductManagedBean the viewProductManagedBean to set
     */
    public void setViewProductManagedBean(ViewProductManagedBean viewProductManagedBean) {
        this.viewProductManagedBean = viewProductManagedBean;
    }

}

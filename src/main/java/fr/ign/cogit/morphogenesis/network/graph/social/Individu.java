package fr.ign.cogit.morphogenesis.network.graph.social;

import java.awt.geom.Point2D;

public class Individu {

  /**
   * Id_individu dans la table des individu parisiens géocodés
   */
  private int id;
  private int idConnectedComponent;
  /**
   * Attributs divers
   */
  private String nom, prenom, profession, num_adr, nom_adr, num_complement,
      qualite, sexe, methode;

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

  public String getPrenom() {
    return prenom;
  }

  public void setPrenom(String prenom) {
    this.prenom = prenom;
  }

  public String getProfession() {
    return profession;
  }

  public void setProfession(String profession) {
    this.profession = profession;
  }

  public String getNum_adr() {
    return num_adr;
  }

  public void setNum_adr(String num_adr) {
    this.num_adr = num_adr;
  }

  public String getNom_adr() {
    return nom_adr;
  }

  public void setNom_adr(String nom_adr) {
    this.nom_adr = nom_adr;
  }

  public String getNum_complement() {
    return num_complement;
  }

  public void setNum_complement(String num_complement) {
    this.num_complement = num_complement;
  }

  public String getQualite() {
    return qualite;
  }

  public void setQualite(String qualite) {
    this.qualite = qualite;
  }

  public String getSexe() {
    return sexe;
  }

  public void setSexe(String sexe) {
    this.sexe = sexe;
  }

  public String getMethode() {
    return methode;
  }

  public void setMethode(String methode) {
    this.methode = methode;
  }

  public Point2D getPosition() {
    return position;
  }

  public void setPosition(Point2D position) {
    this.position = position;
  }

  public Individu(int id, String nom, String prenom, String profession,
      String num_adr, String nom_adr, String num_complement, String qualite,
      String sexe, String methode, Point2D position) {

    this.id = id;
    this.nom = nom;
    this.prenom = prenom;
    this.profession = profession;
    this.num_adr = num_adr;
    this.nom_adr = nom_adr;
    this.num_complement = num_complement;
    this.qualite = qualite;
    this.sexe = sexe;
    this.methode = methode;
    this.position = position;
  }

  public Individu(Individu i) {
    super();
    this.id = i.getId();
    this.nom = i.nom;
    this.prenom = i.prenom;
    this.profession = i.profession;
    this.num_adr = i.num_adr;
    this.nom_adr = i.nom_adr;
    this.num_complement = i.num_complement;
    this.qualite = i.qualite;
    this.sexe = i.sexe;
    this.methode = i.methode;
    this.position = i.position;
  }

  private Point2D position;

  public boolean equals(Individu ind) {
    return (this.id == ind.getId());
  }

  public void setIdConnectedComponent(int idConnectedComponent) {
    this.idConnectedComponent = idConnectedComponent;
  }

  public int getIdConnectedComponent() {
    return idConnectedComponent;
  }

}

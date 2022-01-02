package fr.commons.generique.controller.utils;

/**
 * Created by Zapagon on 25/03/2017.
 */
public final class TagUtils {

	//Tag lors d'erreur qui auraient été catch à aucun niveau
	public static final String CRIT = "CRIT";

	//Tag lors d'erreur dans un catch qui affecte l'appli
	public static final String ERR = "ERR";

	// Tag lors d'erreur dans un catch mais qui n'est pas vital au fonctionnement de l'application
	public static final String WARN = "WARN";

	// Tag lors d'information pour suivre de le déroulement de l'application
	public static final String INFO = "INFO";

	// Log d'information pour suivre finement les étapes généralement pour trouver des bugs
	public static final String DEBUG = "DEBUG";
}
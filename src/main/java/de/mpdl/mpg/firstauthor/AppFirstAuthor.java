package de.mpdl.mpg.firstauthor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Aus einer exportierten Liste mit PuRe Datensätzen im json-Format wird eine Liste im csv-Format
 * generiert mit ID, Titel und Name des 1. Autors von nur den Datensätzen, die als 1. Autor eine
 * Person von einem bestimmten Institut haben.
 * 
 * Für eine schöne Darstellung der Eingabedatei verwende: http://json.parser.online.fr
 */
public class AppFirstAuthor {
  public static String readFile(String fileName) throws IOException {
    return Files.readString(Paths.get(fileName));
  }

  public static void writeFile(String fileName, String content) throws IOException {
    Files.writeString(Paths.get(fileName), content, StandardOpenOption.CREATE);
  }

  public static void main(String[] args) {
    System.out.println("+++ Starte Anwendung +++\n");

    if (args.length != 3) {
      System.out.println("Usage: java -jar firstAuthor.jar IN_FILE OUT_FILE OU");
      return;
    }

    String inFile = args[0];
    String outFile = args[1];
    String ou = args[2];

    System.out.println("INFO: IN_FILE " + inFile);
    System.out.println("INFO: OUT_FILE " + outFile);
    System.out.println("INFO: OU " + ou + "\n");

    StringBuilder sb = new StringBuilder();

    String content = null;
    try {
      content = AppFirstAuthor.readFile(inFile);
      sb.append("ID\tTitel\tName\tOrganisation\n");
    } catch (IOException e) {
      System.out.println("\n##### ERROR #####:\n");
      e.printStackTrace();
      return;
    }

    JSONObject jsonObject = new JSONObject(content);
    JSONArray jsonArrayRecords = jsonObject.getJSONArray("records");
    for (int i = 0; i < jsonArrayRecords.length(); i++) {
      JSONObject jsonObjectRecord = jsonArrayRecords.getJSONObject(i);
      JSONObject jsonObjectData = jsonObjectRecord.getJSONObject("data");
      JSONObject jsonObjectMetaData = jsonObjectData.getJSONObject("metadata");
      try {
        JSONArray jsonArrayCreators = jsonObjectMetaData.getJSONArray("creators");
        if (jsonArrayCreators != null && jsonArrayCreators.length() > 0) {
          JSONObject jsonObjectFirstCreator = jsonArrayCreators.getJSONObject(0);
          JSONObject jsonObjectFirstCreatorPerson = jsonObjectFirstCreator.getJSONObject("person");
          try {
            JSONArray jsonArrayFirstCreatorPersonOrganizations = jsonObjectFirstCreatorPerson.getJSONArray("organizations");
            for (int j = 0; j < jsonArrayFirstCreatorPersonOrganizations.length(); j++) {
              JSONObject jsonObjectFirstCreatorPersonOrganizationsOrganization = jsonArrayFirstCreatorPersonOrganizations.getJSONObject(j);
              JSONArray jsonArrayFirstCreatorPersonOrganizationsOrganizationIdentifierPath =
                  jsonObjectFirstCreatorPersonOrganizationsOrganization.getJSONArray("identifierPath");
              for (int k = 0; k < jsonArrayFirstCreatorPersonOrganizationsOrganizationIdentifierPath.length(); k++) {
                if (ou.equals(jsonArrayFirstCreatorPersonOrganizationsOrganizationIdentifierPath.get(k))) {
                  sb.append(jsonObjectRecord.getString("persistenceId")); // ID
                  sb.append("\t");
                  sb.append(jsonObjectMetaData.getString("title").replaceAll("\n", "").replaceAll("\r", "")); // Titel
                  sb.append("\t");
                  try {
                    sb.append(jsonObjectFirstCreatorPerson.getString("givenName") + " ");
                  } catch (JSONException e) {
                    System.out.println("##### WARN #####: no givenName found for: " + jsonObjectFirstCreatorPerson);
                  }
                  try {
                    sb.append(jsonObjectFirstCreatorPerson.getString("familyName"));
                  } catch (JSONException e) {
                    System.out.println("##### WARN #####: no familyName found for: " + jsonObjectFirstCreatorPerson);
                  }
                  sb.append("\t");
                  sb.append(jsonArrayFirstCreatorPersonOrganizationsOrganizationIdentifierPath.get(k));
                  sb.append(" - ");
                  sb.append(jsonObjectFirstCreatorPersonOrganizationsOrganization.getString("name"));
                  sb.append("\n");
                  break;
                }
              }
            }
          } catch (JSONException e) {
            System.out.println("##### WARN #####: no organizations found for: " + jsonObjectFirstCreatorPerson);
          }
        } else {
          break;
        }
      } catch (JSONException e) {
        System.out.println("##### WARN #####: no creators (person) as first author found for: " + jsonObjectRecord.getString("persistenceId"));
      }
    }

    try {
      AppFirstAuthor.writeFile(outFile, sb.toString());
      System.out.println("\n+++ Beende Anwendung +++");
    } catch (IOException e) {
      System.out.println("\n##### ERROR #####:\n");
      e.printStackTrace();
    }
  }
}

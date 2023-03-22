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
 * generiert mit ID, Titel, Genre, Date und Type, CONE-ID, Name des Autors von nur den Datensätzen,
 * die als Creator einen Autor (Person) mit CONE-ID von einem bestimmten
 * Institut haben.
 * 
 * Für eine schöne Darstellung der Eingabedatei verwende: http://json.parser.online.fr
 */
public class AppAllAuthors {
  public static String readFile(String fileName) throws IOException {
    return Files.readString(Paths.get(fileName));
  }

  public static void writeFile(String fileName, String content) throws IOException {
    Files.writeString(Paths.get(fileName), content, StandardOpenOption.CREATE_NEW);
  }

  public static void main(String[] args) {
    System.out.println("+++ Starte Anwendung +++\n");

    if (args.length != 3) {
      System.out.println("Usage: java -jar allAuthors.jar IN_FILE OUT_FILE OU");
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
      content = AppAllAuthors.readFile(inFile);
      sb.append("ID\tTitel\tGenre\tDate\tType\tCONE-ID\tName\tOrganisation\n");
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
          for (int j = 0; j < jsonArrayRecords.length(); j++) {
            JSONObject jsonObjectCreator = jsonArrayCreators.getJSONObject(j);
            workOnCreator(ou, sb, jsonObjectRecord, jsonObjectMetaData, jsonObjectCreator);
          }
        } else {
          break;
        }
      } catch (JSONException e) {
        System.out.println(
            "##### WARN #####: no creators (person) as author found for: " + jsonObjectRecord.getString("persistenceId"));
      }
    }

    try {
      AppAllAuthors.writeFile(outFile, sb.toString());
      System.out.println("\n+++ Beende Anwendung +++");
    } catch (IOException e) {
      System.out.println("\n##### ERROR #####:\n");
      e.printStackTrace();
    }
  }

  private static void workOnCreator(String ou, StringBuilder sb, JSONObject jsonObjectRecord, JSONObject jsonObjectMetaData,
      JSONObject jsonObjectCreator) {
    if (!"PERSON".equals(jsonObjectCreator.getString("type"))) {
      System.out.println("##### WARN #####: creator is no person: " + jsonObjectCreator);
    } else if (!"AUTHOR".equals(jsonObjectCreator.getString("role"))) {
      System.out.println("##### WARN #####: creator is no author: " + jsonObjectCreator);
    } else {
      workOnPerson(ou, sb, jsonObjectRecord, jsonObjectMetaData, jsonObjectCreator);
    }
  }

  private static void workOnPerson(String ou, StringBuilder sb, JSONObject jsonObjectRecord, JSONObject jsonObjectMetaData,
      JSONObject jsonObjectCreator) {
    JSONObject jsonObjectCreatorPerson = jsonObjectCreator.getJSONObject("person");
    JSONObject jsonObjectCreatorPersonIdentifier;
    String creatorPersonIdentifierType;
    String creatorPersonIdentifierID;
    try {
      jsonObjectCreatorPersonIdentifier = jsonObjectCreatorPerson.getJSONObject("identifier");
      creatorPersonIdentifierType = jsonObjectCreatorPersonIdentifier.getString("type");
      creatorPersonIdentifierID = jsonObjectCreatorPersonIdentifier.getString("id");
      if ("CONE".equals(creatorPersonIdentifierType)) {
        try {
          JSONArray jsonArrayCreatorPersonOrganizations = jsonObjectCreatorPerson.getJSONArray("organizations");
          for (int i = 0; i < jsonArrayCreatorPersonOrganizations.length(); i++) {
            JSONObject jsonObjectCreatorPersonOrganizationsOrganization = jsonArrayCreatorPersonOrganizations.getJSONObject(i);
            JSONArray jsonArrayCreatorPersonOrganizationsOrganizationIdentifierPath =
                jsonObjectCreatorPersonOrganizationsOrganization.getJSONArray("identifierPath");
            writeResult(ou, sb, jsonObjectRecord, jsonObjectMetaData, jsonObjectCreatorPerson, creatorPersonIdentifierID,
                jsonObjectCreatorPersonOrganizationsOrganization, jsonArrayCreatorPersonOrganizationsOrganizationIdentifierPath);
            break;
          }
        } catch (JSONException e) {
          System.out.println("##### WARN #####: no organizations found for: " + jsonObjectCreatorPerson);
        }
      }
    } catch (JSONException e) {
      System.out.println("##### WARN #####: no identifier found for: " + jsonObjectCreatorPerson);
    }
  }

  private static void writeResult(String ou, StringBuilder sb, JSONObject jsonObjectRecord, JSONObject jsonObjectMetaData,
      JSONObject jsonObjectCreatorPerson, String jsonObjectCreatorPersonIdentifierID,
      JSONObject jsonObjectCreatorPersonOrganizationsOrganization, JSONArray jsonArrayCreatorPersonOrganizationsOrganizationIdentifierPath) {
    for (int k = 0; k < jsonArrayCreatorPersonOrganizationsOrganizationIdentifierPath.length(); k++) {
      if (ou.equals(jsonArrayCreatorPersonOrganizationsOrganizationIdentifierPath.get(k))) {
        sb.append(jsonObjectRecord.getString("persistenceId")); // ID
        sb.append("\t");
        sb.append(jsonObjectMetaData.getString("title").replaceAll("\n", "").replaceAll("\r", ""));
        sb.append("\t");
        sb.append(jsonObjectMetaData.getString("genre"));
        sb.append("\t");
        String datePublishedInPrint = null;
        String datePublishedOnline = null;
        String dateAccepted = null;
        String dateSubmitted = null;
        try {
          datePublishedInPrint = jsonObjectMetaData.getString("datePublishedInPrint");
        } catch (JSONException e) {
        }
        if (datePublishedInPrint != null && datePublishedInPrint.trim().length() > 0) {
          sb.append(datePublishedInPrint.trim());
        } else {
          try {
            datePublishedOnline = jsonObjectMetaData.getString("datePublishedOnline");
          } catch (JSONException e) {
          }
          if (datePublishedOnline != null && datePublishedOnline.trim().length() > 0) {
            sb.append(datePublishedOnline.trim());
          } else {
            try {
              dateAccepted = jsonObjectMetaData.getString("dateAccepted");
            } catch (JSONException e) {
            }
            if (dateAccepted != null && dateAccepted.trim().length() > 0) {
              sb.append(dateAccepted.trim());
            } else {
              try {
                dateSubmitted = jsonObjectMetaData.getString("dateSubmitted");
              } catch (JSONException e) {
              }
              if (dateSubmitted != null && dateSubmitted.trim().length() > 0) {
                sb.append(dateSubmitted.trim());
              } else {
                sb.append("n.a.");
              }
            }
          }
        }
        sb.append("\t");
        sb.append("Author\t");
        sb.append(jsonObjectCreatorPersonIdentifierID);
        sb.append("\t");
        try {
          sb.append(jsonObjectCreatorPerson.getString("givenName") + " ");
        } catch (JSONException e) {
          System.out.println("##### WARN #####: no givenName found for: " + jsonObjectCreatorPerson);
        }
        try {
          sb.append(jsonObjectCreatorPerson.getString("familyName"));
        } catch (JSONException e) {
          System.out.println("##### WARN #####: no familyName found for: " + jsonObjectCreatorPerson);
        }
        sb.append("\t");
        sb.append(jsonArrayCreatorPersonOrganizationsOrganizationIdentifierPath.get(k));
        sb.append("\n");
        break;
      }
    }
  }
}

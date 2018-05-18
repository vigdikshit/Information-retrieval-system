import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.*;
import org.apache.commons.lang3.StringUtils;
import com.github.junrar.extract.ExtractArchive;
import com.opencsv.CSVReader;
import java.util.Scanner;


public class assignmentP3 {
	public static void main(String[] args) throws IOException {
		try {
			if (args.length != 5) {
				throw new IllegalArgumentException("Please pass 5 arguments");
			}
			PrintWriter res, postingFile;
			String fileName = "";
			String outputFile = args[1];
			String postings = args[2];
			res = new PrintWriter(new FileWriter(outputFile));
			postingFile = new PrintWriter(new FileWriter(postings));
			final File rar = new File(args[0]);
			final File destFolder = new File(rar.getParent());
			ExtractArchive extractArchive = new ExtractArchive();
			extractArchive.extractArchive(rar, destFolder);
			ArrayList<dict> a = new ArrayList<dict>();
			File folder = new File(rar.getName().replaceAll(".rar", ""));
			File[] listFiles = folder.listFiles();

			PrintWriter resl = new PrintWriter(
					new FileWriter(args[3]));
			
			for (int i = 0; i < listFiles.length; i++) {

				File file = listFiles[i];

				if (file.isFile() && file.getName().endsWith(".html")) {
					fileName = file.getName();

					assignmentP1.trim(folder.getPath() + "/" + fileName, a, i, fileName, resl);

				}
			}

			ArrayList<dict> dictionaryList = new ArrayList<dict>();
			Set<String> uniqueTerms = new HashSet<String>();

			for (dict string : a) {

				if (!uniqueTerms.add(string.term)) {
					for (dict dictionary : dictionaryList) {
						if (dictionary.term.equals(string.term)) {
							dictionary.df = dictionary.df + 1;
						}

					}

				} else {
					string.df = 1;
					dictionaryList.add(string);
				}

			}
			Comparator<dict> sortedList = new Comparator<dict>() {

				@Override
				public int compare(dict o1, dict o2) {
					String term1 = o1.term;
					String term2 = o2.term;

					return term1.compareTo(term2);

				}

			};
			Collections.sort(dictionaryList, sortedList);
			Collections.sort(a, sortedList);

			for (int i = 1; i < dictionaryList.size(); i++) {
				dictionaryList.get(0).offset = 0;
				dictionaryList.get(i).offset = dictionaryList.get(i - 1).offset + dictionaryList.get(i - 1).df;
			}

			for (dict dictionaryL : dictionaryList) {

				res.println(dictionaryL.term + "," + dictionaryL.df + "," + dictionaryL.offset);
			}
			for (dict postingL : a) {
				postingFile.println(postingL.term + "," + postingL.docid + "," + postingL.tf + "," + postingL.documentName);
			}
			res.close();
			resl.close();
			postingFile.close();
			retrieval(args);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	public static void retrieval(String[] args) {

		try {

//			String dictionary = "/home/dikshit/Downloads/dvig1_java_part2/dictionary.csv";
//			String postingList = "/home/dikshit/Downloads/dvig1_java_part2/posting.csv";
//			String docsTable = "/home/dikshit/Downloads/dvig1_java_part2/docsTable.txt";
//			String outputFile = "/home/dikshit/Downloads/dvig1_java_part2/output.txt";
			
			String dictionary = args[1];
			String postingList = args[2];
			String docsTable = args[3];
			String outputFile = args[4];

			CSVReader dictionaryRead = null, postingRead = null;

			dictionaryRead = new CSVReader(new FileReader(dictionary));
			postingRead = new CSVReader(new FileReader(postingList));

			BufferedReader dictionaryReader = new BufferedReader(new FileReader(dictionary));
			// BufferedReader postingRead = new BufferedReader(new FileReader(postingList));
			BufferedReader docsTableRead = new BufferedReader(new FileReader(docsTable));

			PrintWriter output = new PrintWriter(new FileWriter(outputFile));

			dictionaryClass dClass;
			postingClass pClass;
			docsTableClass dTClass, docsClass;

			ArrayList<dictionaryClass> dictionaryArray = new ArrayList<dictionaryClass>();
			ArrayList<postingClass> postingArray = new ArrayList<postingClass>();
			ArrayList<docsTableClass> docsTableArray = new ArrayList<docsTableClass>();

			String[] dictionaryLine;
			String[] postingLine;
			String docsTableLine;

			while ((dictionaryLine = dictionaryRead.readNext()) != null) {
				dClass = new dictionaryClass();
				dClass.terms = dictionaryLine[0];
				dClass.docFrequency = Integer.parseInt(dictionaryLine[1]);
				dClass.termOffset = Integer.parseInt(dictionaryLine[2]);

				dictionaryArray.add(dClass);

			}

			while ((postingLine = postingRead.readNext()) != null) {
				pClass = new postingClass();
				pClass.termPost = postingLine[0];
				pClass.docId = Integer.parseInt(postingLine[1]);
				pClass.termFreq = Integer.parseInt(postingLine[2]);

				postingArray.add(pClass);

			}

			while ((docsTableLine = docsTableRead.readLine()) != null) {
				dTClass = new docsTableClass();
				String[] attributes = docsTableLine.trim().split("\\|\\|");

				dTClass.fileName = attributes[0];
				dTClass.title = attributes[1];
				dTClass.reviewer = attributes[2];
				dTClass.snippet = attributes[3];
				dTClass.rate = attributes[4];

				docsTableArray.add(dTClass);
				
			}
			while (true) {
				Set<Integer> docSet = new HashSet<Integer>();
				Set<Integer> docSetNot = new HashSet<Integer>();
				ArrayList<Integer> dcId = new ArrayList<Integer>();
				ArrayList<Integer> dcIdNot = new ArrayList<Integer>();
				ArrayList<Integer> docIdArrayList = new ArrayList<Integer>();
				ArrayList<Integer> docIdArrayListNot = new ArrayList<Integer>();

				Scanner reader = new Scanner(System.in);
				System.out.println("Enter a query: ");
				String query = reader.nextLine();
				int indexOfDoc;

				String[] querySplit = query.split(" ");
				int y = 0;
				if (querySplit[0].equals("AND")) {
					int andStartIndex = 1;
					int andEndIndex = querySplit.length;
					int andNotStartIndex = 1;
					int andNotEndIndex = querySplit.length;
					// AND loop
					for (int i = 1; i < querySplit.length; i++) {
						if (querySplit[i].equals("AND")) {
							if (querySplit[i + 1].equals("NOT")) {
								andEndIndex = i;
								andNotStartIndex = i + 2;
								break;

							}
						}
					}
					for (int i = andStartIndex; i < andEndIndex; i++) {
						for (dictionaryClass string : dictionaryArray) {
							int offSet = string.termOffset;
							if (string.terms.equals(querySplit[i].toLowerCase())) {
								for (int j = 0; j < string.docFrequency; j++) {

									indexOfDoc = postingArray.get(offSet).docId;
									docIdArrayList.add(indexOfDoc);
									offSet = offSet + 1;
								}

							}
						}

					}
					for (int j = 0; j < docIdArrayList.size(); j++) {
						int x = 0;
						for (int k = j + 1; k < docIdArrayList.size(); k++) {
							if (docIdArrayList.get(j) == docIdArrayList.get(k)) {
								x++;
							}
						}

						if (x == andEndIndex - 2) {
							docSet.add(docIdArrayList.get(j));
						}
					}

					for (int dId : docSet) {
						dcId.add(dId);
					}
					// loops for AND NOT
					if (andNotStartIndex > 1) {
						for (int i2 = andNotStartIndex; i2 < andNotEndIndex; i2++) {
							for (dictionaryClass string : dictionaryArray) {
								int offSet = string.termOffset;
								if (string.terms.equals(querySplit[i2].toLowerCase())) {
									for (int j = 0; j < string.docFrequency; j++) {

										indexOfDoc = postingArray.get(offSet).docId;
										docIdArrayListNot.add(indexOfDoc);
										offSet = offSet + 1;
									}

								}
							}

						}

						for (int j = 0; j < docIdArrayListNot.size(); j++) {
							docSetNot.add(docIdArrayListNot.get(j));
						}
						for (int dId : docSetNot) {
							dcIdNot.add(dId);
						}
					}
					dcId.removeAll(dcIdNot);
					ArrayList<docsTableClass> ratePos = new ArrayList<docsTableClass>();
					ArrayList<docsTableClass> rateNeg = new ArrayList<docsTableClass>();
					ArrayList<docsTableClass> rateNA = new ArrayList<docsTableClass>();

					for (int j = 0; j < dcId.size(); j++) {

						int docIds = dcId.get(j);
						docsClass = new docsTableClass();
						docsClass.fileName = docsTableArray.get(docIds).fileName;
						docsClass.title = docsTableArray.get(docIds).title;
						docsClass.reviewer = docsTableArray.get(docIds).reviewer;
						docsClass.snippet = docsTableArray.get(docIds).snippet;
						docsClass.rate = docsTableArray.get(docIds).rate;

						if (docsClass.rate.equals("P")) {
							ratePos.add(docsClass);
						} else if (docsClass.rate.equals("N")) {
							rateNeg.add(docsClass);
						} else
							rateNA.add(docsClass);

					}
					Comparator<docsTableClass> sortRate = new Comparator<docsTableClass>() {

						public int compare(docsTableClass o1, docsTableClass o2) {
							String rate1 = o1.fileName;
							String rate2 = o2.fileName;

							return rate1.compareTo(rate2);

						}

					};

					Collections.sort(ratePos, sortRate);
					Collections.sort(rateNeg, sortRate);
					Collections.sort(rateNA, sortRate);
					System.out.println("Query: " + query);
					output.println("\n" + "Query: " + query);
					if(dcId.size() == 0) {
						System.out.println("No Results");
						output.println("No Results");
					}
					for (docsTableClass finalDoc : ratePos) {
						System.out.println("Filename: " + finalDoc.fileName);
						System.out.println("Title: " + finalDoc.title);
						System.out.println("Reviewer: " + finalDoc.reviewer);
						System.out.println("Snippet: " + finalDoc.snippet);
						System.out.println("Rate: " + finalDoc.rate);
						System.out.println("");
						output.println("\n" + finalDoc.fileName + "\n" + finalDoc.title + "\n" + finalDoc.reviewer + "\n"
								+ finalDoc.snippet + "\n" + finalDoc.rate);
					}
					for (docsTableClass finalDoc : rateNeg) {
						System.out.println("Filename: " + finalDoc.fileName);
						System.out.println("Title: " + finalDoc.title);
						System.out.println("Reviewer: " + finalDoc.reviewer);
						System.out.println("Snippet: " + finalDoc.snippet);
						System.out.println("Rate: " + finalDoc.rate);
						System.out.println("");
						output.println("\n" + finalDoc.fileName + "\n" + finalDoc.title + "\n" + finalDoc.reviewer + "\n"
								+ finalDoc.snippet + "\n" + finalDoc.rate);
					}
					for (docsTableClass finalDoc : rateNA) {
						System.out.println("Filename: " + finalDoc.fileName);
						System.out.println("Title: " + finalDoc.title);
						System.out.println("Reviewer: " + finalDoc.reviewer);
						System.out.println("Snippet: " + finalDoc.snippet);
						System.out.println("Rate: " + finalDoc.rate);
						System.out.println("");
						output.println("\n" + finalDoc.fileName + "\n" + finalDoc.title + "\n" + finalDoc.reviewer + "\n"
								+ finalDoc.snippet + "\n" + finalDoc.rate);
					}

				}

				else if (querySplit[0].equals("OR")) {

					for (int i = 1; i < querySplit.length; i++) {

						for (dictionaryClass string : dictionaryArray) {
							int offSet = string.termOffset;
							if (string.terms.equals(querySplit[i].toLowerCase())) {

								for (int j = 0; j < string.docFrequency; j++) {

									indexOfDoc = postingArray.get(offSet).docId;

									docSet.add(indexOfDoc);
									offSet = offSet + 1;

								}

							}
						}

					}
					for (int dId : docSet) {
						dcId.add(dId);
						// System.out.println(dcId);

					}
					ArrayList<docsTableClass> ratePos = new ArrayList<docsTableClass>();
					ArrayList<docsTableClass> rateNeg = new ArrayList<docsTableClass>();
					ArrayList<docsTableClass> rateNA = new ArrayList<docsTableClass>();

					for (int j = 0; j < dcId.size(); j++) {

						int docIds = dcId.get(j);
						docsClass = new docsTableClass();
						docsClass.fileName = docsTableArray.get(docIds).fileName;
						docsClass.title = docsTableArray.get(docIds).title;
						docsClass.reviewer = docsTableArray.get(docIds).reviewer;
						docsClass.snippet = docsTableArray.get(docIds).snippet;
						docsClass.rate = docsTableArray.get(docIds).rate;

						if (docsClass.rate.equals("P")) {
							ratePos.add(docsClass);
						} else if (docsClass.rate.equals("N")) {
							rateNeg.add(docsClass);
						} else
							rateNA.add(docsClass);

					}
					Comparator<docsTableClass> fileSort = new Comparator<docsTableClass>() {

						public int compare(docsTableClass o1, docsTableClass o2) {
							String file1 = o1.fileName;
							String file2 = o2.fileName;

							return file1.compareTo(file2);

						}

					};

					Collections.sort(ratePos, fileSort);
					Collections.sort(rateNeg, fileSort);
					Collections.sort(rateNA, fileSort);
					System.out.println("Query: " + query);
					output.println("\n" + "Query: " + query);
					if(dcId.size() == 0) {
						System.out.println("No Results");
						output.println("No Results");
					}
					for (docsTableClass finalDoc : ratePos) {
						System.out.println("Filename: " + finalDoc.fileName);
						System.out.println("Title: " + finalDoc.title);
						System.out.println("Reviewer: " + finalDoc.reviewer);
						System.out.println("Snippet: " + finalDoc.snippet);
						System.out.println("Rate: " + finalDoc.rate);
						System.out.println("");

						output.println("\n" + finalDoc.fileName + "\n" + finalDoc.title + "\n" + finalDoc.reviewer + "\n"
								+ finalDoc.snippet + "\n" + finalDoc.rate);
					}
					for (docsTableClass finalDoc : rateNeg) {

						System.out.println("Filename: " + finalDoc.fileName);
						System.out.println("Title: " + finalDoc.title);
						System.out.println("Reviewer: " + finalDoc.reviewer);
						System.out.println("Snippet: " + finalDoc.snippet);
						System.out.println("Rate: " + finalDoc.rate);
						System.out.println("");
						output.println("\n" + finalDoc.fileName + "\n" + finalDoc.title + "\n" + finalDoc.reviewer + "\n"
								+ finalDoc.snippet + "\n" + finalDoc.rate);
					}
					for (docsTableClass finalDoc : rateNA) {

						System.out.println("Filename: " + finalDoc.fileName);
						System.out.println("Title: " + finalDoc.title);
						System.out.println("Reviewer: " + finalDoc.reviewer);
						System.out.println("Snippet: " + finalDoc.snippet);
						System.out.println("Rate: " + finalDoc.rate);
						System.out.println("");
						output.println("\n" + finalDoc.fileName + "\n" + finalDoc.title + "\n" + finalDoc.reviewer + "\n"
								+ finalDoc.snippet + "\n" + finalDoc.rate);
					}
					

				}
				else {
					System.out.println("Query: " + query);
					System.out.println("No Results");
					output.println("\n" + "Query: " + query);
					output.println("No Results");
				}
				if (querySplit[0].toUpperCase().equals("EXIT")) {
					break;
				}
			}

			dictionaryRead.close();
			postingRead.close();
			docsTableRead.close();
			dictionaryReader.close();
			output.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}


class dictionaryClass {

	String terms;
	int docFrequency;
	int termOffset;
}

class postingClass {
	int docId;
	int termFreq;
	String termPost;
}

class docsTableClass {
	String fileName;
	String title;
	String reviewer;
	String snippet;
	String rate;
}


class dict {
	public int docid;
	public String term;
	public int tf;
	public int df;
	public int offset;
	public String documentName;
}

class assignmentP1 {
	public static void trim(String inputFile, ArrayList<dict> abc, int docCount, String docName, PrintWriter resl) {
		try {

			int termF = 1;
			String terms;
			ArrayList<String> list = new ArrayList<String>();
			FileReader fr;
			BufferedReader br;
			File f;

			f = new File(inputFile);
			if (!f.exists()) {
				throw new IllegalArgumentException("File does not exist");
			}
			fr = new FileReader(f);
			br = new BufferedReader(fr);

			String Doc = "";
			while ((terms = br.readLine()) != null) {
				Doc = Doc + "" + terms;

				terms = " " + terms.toLowerCase() + " "; // Convert to lower case

				terms = terms.replaceAll("\\<.*?>", " "); // To remove HTML tags

				terms = terms.replaceAll("(, )|(,)|(\\. )|(; )|(! )|(: )", " "); // To remove
				// period,comma,semi-colon,exclamation,colon
				// followed by space
				terms = terms.replaceAll("([^ ])(-)([^ ])", "$1 $3"); // To remove hyphen if present between terms

				terms = terms.replaceAll("(\\.$)", " "); // To remove period if on end of a line

				terms = terms.replaceAll("\\? ", " "); // To remove question mark followed by space

				terms = terms.replaceAll("'", ""); // To remove apostrophes

				terms = terms.replaceAll("\"", "");

				terms = terms.trim().replaceAll(
						" an | a | a, | and | an, | is | is, | the | by | from | for | hence | of | with | within | who | when | where | why | how | whom | have | had | has | for | but | does | do | then | done ",
						" "); // To remove all the stop words mentioned

				String[] termsArray = terms.split(" ");

				for (String stemmedTerms : termsArray) {
					if (stemmedTerms.startsWith("(")) {
						stemmedTerms = stemmedTerms.replace("(", "");
					}
					if (stemmedTerms.startsWith("[")) {
						stemmedTerms = stemmedTerms.replace("[", "");
					}
					if (stemmedTerms.startsWith("'")) {
						stemmedTerms = stemmedTerms.replace("'", "");
					}
					if (stemmedTerms.startsWith("\"")) {
						stemmedTerms = stemmedTerms.replace("\"", "");
					}
					if (stemmedTerms.endsWith(")") || stemmedTerms.endsWith(") ")) {
						stemmedTerms = stemmedTerms.replace(")", "");
					}
					if (stemmedTerms.endsWith("]") || (stemmedTerms.endsWith("] "))) {
						stemmedTerms = stemmedTerms.replace("]", "");
					}
					if (stemmedTerms.endsWith("'")) {
						stemmedTerms = stemmedTerms.replace("'", "");
					}
					if (stemmedTerms.endsWith("\"")) {
						stemmedTerms = stemmedTerms.replace("\"", "");
					}
					if (stemmedTerms.endsWith("ies")
							&& !((stemmedTerms.endsWith("eies")) || (stemmedTerms.endsWith("aies")))) {
						int len = stemmedTerms.length();
						stemmedTerms = stemmedTerms.substring(0, len - 3) + "y";

					}
					if (stemmedTerms.endsWith("es") && !((stemmedTerms.endsWith("aes"))
							|| (stemmedTerms.endsWith("ees")) || (stemmedTerms.endsWith("oes")))) {
						int len = stemmedTerms.length();
						stemmedTerms = stemmedTerms.substring(0, len - 2) + "e";

					}
					if (stemmedTerms.endsWith("s")
							&& !((stemmedTerms.endsWith("us")) || (stemmedTerms.endsWith("ss")))) {
						int len = stemmedTerms.length();
						stemmedTerms = stemmedTerms.substring(0, len - 1);
					}

					if (stemmedTerms.length() == 1) {
						stemmedTerms = "";
					}

					list.add(stemmedTerms);

				}

			}

			Set<String> unique = new HashSet<String>(list);
			String[] ASR = docTable(Doc);
			
			resl.println(docName + "||" + ASR[0] + "||" + ASR[1] + "||" + ASR[2] + "||" + ASR[3]);

			for (String temp : unique) {
				if (!temp.isEmpty()) {

					termF = Collections.frequency(list, temp);
					

					dict d = new dict();
					d.docid = docCount;
					d.term = temp;
					d.tf = termF;
					d.offset = 0;
					d.documentName = docName;
					abc.add(d);

				}

			}

			fr.close();
			br.close();
			// out.close();
		

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static String[] docTable(String doc) {

		String[] ASR = new String[4];
		Words words = new Words();

		ASR[0] = StringUtils.substringBetween(doc, "<TITLE>", "</TITLE>");

		String s = StringUtils.substringBetween(doc, "<A HREF=\"/ReviewsBy?", "/A>");
		ASR[1] = StringUtils.substringBetween(s, ">", "<");

		if (doc.contains("Capsule review:") || doc.contains(" Capsule review:")) {

			int wordCount = 0;

			String s1 = StringUtils.substringAfter(doc, "Capsule review:").trim();
			s1 = s1.replaceAll("\\<.*?>", " ");
			String[] s2 = s1.split(" ");

			for (int i = 0; i < s2.length; i++) {
				if (!s2[i].isEmpty()) {
					ASR[2] += s2[i] + " ";
					ASR[2] = ASR[2].replace("null", "");
					wordCount++;
					if (wordCount == 50) {
						break;
					}
				}

			}

		}

		if (!(doc.contains("Capsule review:") || doc.contains(" Capsule review:"))) {
			String s1 = StringUtils.substringAfter(doc, "</HEAD>");

			s1 = s1.replaceAll("\\<.*?>", " ");

			String[] s2 = s1.split(" ");

			int wordCount = 0;

			for (int i = 0; i < s2.length; i++) {
				if (!s2[i].isEmpty()) {
					ASR[2] += s2[i] + " ";
					ASR[2] = ASR[2].replace("null", "");
					wordCount++;
					if (wordCount == 50) {
						break;
					}
				}

			}

		}
		if (doc.contains("-4 to +4 scale")) {
			int rt = doc.indexOf("-4 to +4");

			String st = StringUtils.substringBefore(doc, "-4 to +4");

			for (int i = rt; i > 0; i--) {
				if (Character.isDigit(doc.charAt(i))) {
					String rate = doc.substring(i - 1, i + 1);

					if (rate.contains("+")) {
						ASR[3] = "P";
					} else if (rate.contains("-")) {
						ASR[3] = "N";
					} else if (rate.contains(" ")) {
						ASR[3] = "P";
					} else if (rate.contains(".")) {
						doc = doc.substring(i - 3, i + 1);

						if (doc.contains("+")) {
							ASR[3] = "P";
						}
						if (doc.contains("-")) {
							ASR[3] = "N";
						}
					}
					// ASR[3] = doc;
					break;
				}

			}

		} else if (!(doc.trim().contains("-4 to +4 scale"))) {

			String rating = StringUtils.substringAfter(doc, "Capsule review:");
			rating = rating.replaceAll("\\<.*?>", " ");
			rating = rating.toLowerCase();
			int countP = 0;
			int countN = 0;
			int dif;
			for (String string : words.pWords) {
				if (rating.contains(string.toLowerCase())) {
					countP++;
				}
			}
			for (String string : words.nWords) {
				if (rating.contains(string.toLowerCase())) {
					countN++;
				}
			}
			dif = countP - countN;

			if (dif >= 0) {
				if (countP == 0 && countN == 0) {
					ASR[3] = "NA";
				} else
					ASR[3] = "P";
			}
			if (dif < 0) {
				ASR[3] = "N";
			}
		} else {
			ASR[3] = "NA";
		}
		return ASR;
	}

}

class Words {
	String[] pWords = { "ACQUAINT", "ACQUAINTANCE", "ACQUIT", "ACQUITTAL", "ACTUAL#1", "ACTUAL#2", "ACTUALITY",
			"ADAMANT", "ADAPTABILITY", "ADAPTABLE", "ADAPTATION", "ADAPTIVE", "ADEPT", "ADEPTNESS", "ADEQUATE",
			"ADHERENCE", "ADHERENT", "ADHESION", "ADHESIVE", "ADJUNCT", "ADJUST#2", "ADJUSTABLE", "ADJUSTMENT",
			"ADMIRABLE", "ADMIRATION", "ADMIRE", "ADMIRER", "ADMIT#1", "ADMIT#2", "ADMIT#3", "ADMITTANCE", "ADORABLE",
			"ADORE", "ADORN", "ADORNMENT", "ADROIT", "ADROITLY", "ADULATION", "ADULT#2", "ADVANCE#1", "ADVANCE#2",
			"ADVANCE#3", "ADVANCEMENT", "ADVANTAGE", "ADVANTAGEOUS", "ADVENT", "ADVENTURESOME", "ADVENTUROUS",
			"ADVISABLE", "ADVOCACY", "AFFABILITY", "AFFABLE", "AFFECTION", "AFFECTIONATE", "AFFILIATE", "AFFILIATION",
			"AFFINITY", "AFFIRM", "AFFIRMATION", "AFFIRMATIVE", "AFFIX", "AFFLUENCE", "AFFLUENT", "AFLOAT", "AGGREGATE",
			"AGGREGATION", "AGILE", "AGILITY", "AGREEABLE", "AGREEMENT", "AID#1", "AID#2", "ALERTNESS", "ALIGHT",
			"ALIVE", "ALLEGIANCE", "ALLEVIATE", "ALLIANCE", "ALLIED", "ALLIES", "ALLOW#1", "ALLOWABLE", "ALLOWANCE",
			"ALLURE", "ALLY#1", "ALMIGHTY", "ALTRUISTIC", "AMAZEMENT", "AMAZING", "AMELIORATE", "AMENABLE", "AMENITY",
			"AMIABILITY", "AMIABLE", "AMICABLE", "AMNESTY", "AMOUR", "AMPLE", "AMPLIFY", "AMPLY", "AMUSE", "AMUSEMENT",
			"ANGEL", "ANGELIC", "ANOINT", "APOCALYPSE", "APPEAL#3", "APPEASE", "APPEND", "APPLAUD", "APPLAUSE",
			"APPOINT#1", "APPOINT#2", "APPRECIABLE", "APPRECIATE", "APPRECIATION", "APPRECIATIVE", "APPREHEND",
			"APPROACH#1", "APPROACH#2", "APPROPRIATE#1", "APPROVAL", "APPROVE#1", "APPROVE#2", "APT", "APTITUDE",
			"ARBITER", "ARBITRATE", "ARBITRATION", "ARDENT", "ARISEN", "ARISTOCRACY", "ARISTOCRAT", "ARISTOCRATIC",
			"ARREST#3", "ART#1", "ART#4", "ASCRIBE", "ASPIRATION", "ASPIRE", "ASSENT", "ASSERTION", "ASSET", "ASSIST#1",
			"ASSIST#2", "ASSISTANCE", "ASSISTANT", "ASSOCIATE#1", "ASSOCIATE#2", "ASSOCIATE#3", "ASSOCIATION#1",
			"ASSOCIATION#2", "ASSURANCE", "ASSURE#1", "ASSURE#2", "ASSURE#3", "ASSUREDLY", "ASTOUND", "ASTUTE",
			"ATHLETIC", "ATTACHMENT", "ATTAIN", "ATTAINMENT", "ATTEND#1", "ATTENDANCE", "ATTENTIVE", "ATTRACT",
			"ATTRACTION", "ATTRACTIVE", "ATTRACTIVENESS", "ATTUNE", "AUDIBLE", "AUGMENT", "AUGMENTATION", "AUSPICIOUS",
			"AUTHENTIC", "AUTHENTICITY", "AUTHORITATIVE", "AUTHORITY", "AUTONOMOUS", "AVAILABILITY", "AVID", "AWARD#1",
			"AWARD#2", "AWARE", "AWARENESS", "BACK#2", "BACK#5", "BACKBONE", "BACKER", "BACKING", "BALL#5", "BALL#6",
			"BALMY", "BAPTISM", "BAPTIZE", "BARGAIN", "BASIC#1", "BASIC#2", "BEACON", "BEAUTEOUS", "BEAUTIFY", "BEAUTY",
			"BEFIT", "BEFITTING", "BEFRIEND", "BEHALF", "BELOVED", "BENEFACTOR", "BENEFICENT", "BENEFICIAL",
			"BENEFICIARY", "BENEFIT#1", "BENEFIT#2", "BENEFIT#3", "BENEVOLENCE", "BENEVOLENT", "BENIGN", "BEQUEATH",
			"BEST", "BESTOW", "BETROTH", "BETROTHAL", "BETTER#1", "BETTER#2", "BLAMELESS", "BLESS#1", "BLESS#2",
			"BLESS#3", "BLISS", "BLISSFUL", "BLITHE", "BLOOM", "BLOSSOM", "BOARD#8", "BOLDNESS", "BOLSTER", "BONNY",
			"BONUS", "BOOM", "BOOST#1", "BOOST#2", "BOUNDLESS", "BOUNTIFUL", "BRAINY", "BRAVE", "BRAVERY",
			"BREADWINNER", "BRIGHT", "BRIGHTNESS", "BRILLIANCE", "BRILLIANT", "BROTHERHOOD", "BROTHERLY", "BUOYANT",
			"BUY#2", "CALL#4", "CALM#1", "CALM#2", "CALM#3", "CALMNESS", "CANDID", "CANDOR", "CAPABILITY", "CAPABLE",
			"CAPITALIZE", "CAPTIVATION", "CARE#1", "CARE#2", "CAREFREE", "CAREFUL", "CARESS#1", "CARESS#2", "CASUAL",
			"CEASELESS", "CELEBRATE", "CELEBRATION", "CELEBRITY", "CEREMONIAL", "CHAMP", "CHAMPION", "CHAMPIONSHIP",
			"CHARISMA", "CHARITABLE", "CHARITY", "CHARM#1", "CHARM#2", "CHASTE", "CHEER", "CHEERFUL", "CHEERY",
			"CHERISH#1", "CHERISH#2", "CHERUB", "CHIC", "CHIVALROUS", "CHIVALRY", "CHOSEN#2", "CHUM", "CIVIL#1",
			"CIVILITY", "CIVILIZATION", "CIVILIZE#1", "CIVILIZE#2", "CLARIFY", "CLARITY", "CLASSIC", "CLEAN#1",
			"CLEAN#3", "CLEANLINESS", "CLEANSE", "CLEAR#1", "CLEAR#2", "CLEAR#3", "CLEAR#4", "CLEAR#5", "CLEARNESS",
			"CLEVER", "CLOSENESS", "CLOUT", "CO-OPERATION", "COAX", "CODDLE", "COEXISTENCE", "COGENT", "COGNIZANCE",
			"COGNIZANT", "COHERENT", "COHESION", "COHORT", "COINCIDENT", "COLLABORATE", "COLLABORATION", "COLLEAGUE",
			"COLOSSAL", "COMEBACK", "COMEDY", "COMELY", "COMESTIBLE", "COMFORT#1", "COMFORT#2", "COMFORT#3",
			"COMFORTABLE", "COMIC", "COMICAL", "COMMEMORATE", "COMMEMORATION", "COMMENCEMENT", "COMMEND", "COMMENDABLE",
			"COMMENDATION", "COMMENSURATE", "COMMISSION", "COMMITMENT", "COMMODIOUS", "COMMON#1", "COMMON#4",
			"COMMONSENSE", "COMMUNAL", "COMMUNE", "COMMUNICATE#1", "COMMUNICATE#2", "COMMUNICATIVE", "COMMUNION",
			"COMMUNITY", "COMPANION", "COMPANIONSHIP", "COMPANY#2", "COMPASSION", "COMPASSIONATE", "COMPATIBLE",
			"COMPENSATE", "COMPENSATION", "COMPETENCE", "COMPETENCY", "COMPETENT", "COMPLEMENT#1", "COMPLEMENT#2",
			"COMPLEMENTARY", "COMPLETE#1", "COMPLETE#2", "COMPLETENESS", "COMPLETION", "COMPLIANCE", "COMPLIMENT",
			"COMPOSURE", "COMPREHEND", "COMPREHENSION", "COMPREHENSIVE", "COMPROMISE#1", "COMPROMISE#2", "CONCESSION",
			"CONCLUSIVE", "CONCUR", "CONDONE", "CONDUCIVE", "CONFEDERATION", "CONFER", "CONFIDANT", "CONFIDE",
			"CONFIDENCE#1", "CONFIDENT", "CONGENIAL", "CONGRATULATE", "CONGRATULATION", "CONGRATULATORY", "CONGREGATE",
			"CONJUNCTION", "CONJURE", "CONNECT", "CONQUER", "CONQUEROR", "CONQUEST", "CONSCIENCE", "CONSCIENTIOUS",
			"CONSENSUS", "CONSENT#1", "CONSENT#2", "CONSERVE", "CONSIDER#1", "CONSIDER#2", "CONSIDERATE",
			"CONSIDERATION#1", "CONSIGN", "CONSISTENCY", "CONSISTENT", "CONSOLE", "CONSOLIDATE", "CONSTANCY",
			"CONSTRUCTIVE", "CONSULT", "CONSULTATION", "CONSUMMATE", "CONTACT#1", "CONTACT#2", "CONTENT#3", "CONTENT#4",
			"CONTENT#5", "CONTENTMENT", "CONTINUITY", "CONTRIBUTE#1", "CONTRIBUTE#2", "CONTRIBUTION", "CONTRIBUTOR",
			"CONVENE", "CONVICTION", "CONVINCE#1", "CONVINCE#2", "COOPERATE", "COOPERATION", "COOPERATIVE#1",
			"COOPERATIVE#2", "COOPERATIVE#3", "COORDINATE", "COORDINATION", "CORDIAL", "CORRECT#1", "CORRECT#2",
			"CORRECT#3", "CORRECTION", "COUNCIL", "COUNSEL#1", "COUNSEL#2", "COURAGE", "COURAGEOUS", "COURTEOUS",
			"COURTESY", "COURTLY", "COVENANT", "COZY", "CREATE", "CREATIVE", "CREATIVITY", "CREDENTIALS", "CREDIBILITY",
			"CREDIBLE", "CREDIT#2", "CREDIT#4", "CRUSADE", "CRUSADER", "CUDDLE", "CULMINATE", "CULMINATION",
			"CULTIVATE#1", "CULTIVATE#2", "CULTIVATION", "CULTURE#2", "CUPID", "CURE#1", "CURE#2", "CURTSEY", "CUTE",
			"DANCE#1", "DANCE#2", "DANCE#3", "DARE", "DARING", "DARLING", "DAUNTLESS", "DAWN#2", "DAZZLE", "DEAL#1",
			"DEAR#1", "DEAR#2", "DECENCY", "DECENT", "DECIPHER", "DECORATE", "DECORATION", "DECORATIVE", "DEDICATE#1",
			"DEDICATE#2", "DEDICATION", "DEDUCE", "DEFEND", "DEFENDER", "DEFENSE", "DEFERENCE", "DEFINITIVE",
			"DELICACY", "DELICATE", "DELIGHT#1", "DELIGHT#2", "DELIGHTFUL", "DEPENDABILITY", "DEPENDABLE", "DESCRY",
			"DESERVE", "DESERVEDLY", "DESIRABLE", "DESIROUS", "DEVOTE#1", "DEVOTE#2", "DEVOTION", "DEVOUT", "DEXTERITY",
			"DIG#1", "DIGNIFIED", "DIGNIFY", "DIGNITY", "DILIGENT", "DISCERN", "DISCREET", "DISCRETION", "DISCUSS",
			"DISTINCT", "DISTINCTION", "DISTINCTIVE", "DISTINGUISH#1", "DISTINGUISH#2", "DISTINGUISHED", "DIVINE#1",
			"DIVINE#2", "DIVINITY", "DOMINANCE", "DONATE", "DONATION", "DOTE", "DOUBTLESS", "DREAMLAND", "DURABILITY",
			"DURABLE", "DYNAMIC", "EAGER", "EAGERNESS", "EARNEST", "EARNESTNESS", "EASE#1", "EASE#2", "EASY#1",
			"EASY#2", "EASY#3", "EASY#4", "ECONOMIZE", "ECSTASY", "ECSTATIC", "EDIBLE", "EDUCATED#1", "EDUCATION",
			"EDUCATIONAL", "EFFECTIVE", "EFFECTIVENESS", "EFFICACY", "EFFICIENCY", "EFFICIENT", "ELABORATE",
			"ELABORATION", "ELATE", "ELEGANCE", "ELEGANT", "ELOQUENT", "EMBELLISH", "EMBRACE#1", "EMBRACE#2",
			"EMINENCE", "EMINENT", "EMPATHY", "EMPOWER", "EMPOWERMENT", "ENABLE", "ENCHANT#1", "ENCHANT#2",
			"ENCHANTMENT", "ENCOURAGE#1", "ENCOURAGE#2", "ENCOURAGEMENT", "ENDEAR", "ENDORSE", "ENDOW", "ENDURANCE",
			"ENERGETIC", "ENERGIZE", "ENGAGE#5", "ENHANCE", "ENHANCEMENT", "ENJOY", "ENJOYABLE", "ENJOYMENT",
			"ENLIGHTEN#1", "ENLIGHTEN#2", "ENLIGHTENMENT", "ENRICH", "ENRICHMENT", "ENSEMBLE", "ENSURE", "ENTERTAIN#1",
			"ENTERTAIN#2", "ENTERTAINMENT", "ENTHUSIASM", "ENTHUSIASTIC", "ENTREPRENEURIAL", "ENTRUST", "ENVISION",
			"EQUALITY", "EQUITABLE", "EQUITY", "ESSENTIAL#1", "ESSENTIAL#2", "ESTABLISH#1", "ESTABLISH#2", "ESTEEM",
			"ETHICAL", "ETHICS", "ETIQUETTE", "EVEN#4", "EVEN#5", "EVERLASTING", "EXACT#1", "EXACT#2", "EXALT", "EXCEL",
			"EXCELLENCE", "EXCELLENT", "EXCITE#1", "EXCITE#2", "EXCITED#2", "EXCITEDNESS", "EXCITEMENT", "EXERTION",
			"EXHILARATION", "EXOTIC", "EXPERIENCE#1", "EXPERIENCE#3", "EXPERT#1", "EXPERT#2", "EXQUISITE", "EXTOL",
			"EXTRAORDINARY", "EXTRAVAGANCE", "EXUBERANCE", "EXUBERANT", "EXULT", "EXULTATION", "EYE#3", "FABULOUS",
			"FACILITATE", "FACTUAL", "FAIR#1", "FAIR#4", "FAIR#5", "FAIRNESS", "FAITH#1", "FAITH#2", "FAITHFUL",
			"FAITHFULNESS", "FAME", "FAMILIAR", "FAMILIARITY", "FAMILIARIZE", "FAMOUS", "FANCY", "FANTASTIC", "FANTASY",
			"FARSIGHTED", "FASCINATION", "FASHIONABLE", "FAVOR#1", "FAVOR#2", "FAVOR#3", "FAVOR#4", "FAVOR#5",
			"FAVORABLE", "FAVORITE", "FEARLESS", "FEASIBLE", "FEAST#1", "FEAST#2", "FELLOW#2", "FELLOWSHIP", "FERTILE",
			"FERVENT", "FERVOR", "FESTIVAL", "FESTIVE", "FESTIVITY", "FIDELITY", "FIERY", "FILIAL", "FILL#4", "FINE#1",
			"FINE#2", "FINE#3", "FINE#4", "FINE#5", "FIRMNESS", "FIT#2", "FIT#5", "FITNESS", "FLAIR", "FLASHY",
			"FLATTER", "FLATTERY", "FLAUNT", "FLAWLESS", "FLEXIBLE", "FLIRT", "FLOURISH", "FLUENT", "FOND#1", "FOND#2",
			"FOND#3", "FOND#4", "FOND#5", "FOND#6", "FOND#7", "FONDNESS", "FOREMOST", "FORESIGHT", "FORGAVE", "FORGIVE",
			"FORGIVEN", "FORGIVENESS", "FORMALITY", "FORTIFY", "FORTITUDE", "FORTUNATE", "FORWARD#1", "FOSTER",
			"FRAGRANT", "FRANK", "FREE#1", "FREE#2", "FREE#3", "FREE#4", "FREE#5", "FREEDOM", "FRESH", "FRIEND",
			"FRIENDLY", "FRIENDSHIP", "FROLIC", "FRUGAL", "FRUITFUL", "FRUITION", "FULFILL", "FULFILLMENT", "FULLNESS",
			"FUN#1", "FUNNY#1", "FUNNY#2", "FUNNY#3", "GAIETY", "GAILY", "GAIN#1", "GAIN#2", "GALLANT", "GALLANTRY",
			"GAME#3", "GARNISH", "GAY", "GENERATE", "GENEROSITY", "GENEROUS", "GENIAL", "GENIUS", "GENTLE", "GENUINE",
			"GIDDY", "GIFT", "GIFTED", "GIVE#1", "GIVE#4", "GLAD#1", "GLAD#2", "GLADDEN", "GLADNESS", "GLAMOROUS",
			"GLAMOUR", "GLEAM#1", "GLEAM#2", "GLEAN", "GLEE", "GLIMMER", "GLISTEN#1", "GLISTEN#2", "GLITTER#1",
			"GLITTER#2", "GLORIFY", "GLORIOUS", "GLORY", "GLOSSY", "GLOW#1", "GLOW#2", "GODLIKE", "GODLINESS", "GOLD",
			"GOLDEN", "GOOD#1", "GOODBYE", "GOODNESS", "GORGEOUS", "GRACE", "GRACEFUL", "GRACIOUS", "GRADUATION",
			"GRAND", "GRANDEUR", "GRATEFUL", "GRATIFICATION", "GRATIFY", "GRATITUDE", "GREAT#1", "GREAT#2", "GREAT#3",
			"GREATNESS", "GREET#1", "GREET#2", "GROOM", "GUARANTEE#1", "GUARANTEE#2", "GUARDIAN", "GUIDE#1", "GUIDE#2",
			"GUIDE#4", "GUSTO", "HALLOWED", "HAND#9", "HANDSOME", "HANDY", "HAPPINESS", "HAPPY#1", "HAPPY#2", "HAPPY#3",
			"HAPPY#4", "HARDY", "HARMLESS", "HARMONIOUS", "HARMONIZE", "HARMONY", "HARNESS", "HAVEN", "HEAL", "HEALTH",
			"HEALTHFUL", "HEALTHY#1", "HEALTHY#2", "HEALTHY#3", "HEART#4", "HEARTILY", "HEAVEN", "HEAVENLY", "HELP#1",
			"HELP#2", "HELP#4", "HELPFUL", "HERO", "HEROIC", "HEROINE", "HEROISM", "HIGHLIGHT", "HILARIOUS", "HIT#5",
			"HOLY", "HOMAGE", "HOME", "HONEST#1", "HONEST#2", "HONEST#3", "HONEYMOON", "HONOR#1", "HONOR#2", "HONOR#3",
			"HONORABLE", "HOPE#1", "HOPE#2", "HOPEFUL", "HOSPITABLE", "HUG", "HUMAN", "HUMANITARIAN", "HUMANITY",
			"HUMBLE", "HUMILITY", "HUMOR", "HUMOROUS", "HYGIENE", "IDEAL#1", "IDEAL#2", "IDEAL#3", "IDEALISM", "IDOL",
			"IDOLIZE", "ILLUMINATE", "ILLUSTRIOUS", "IMAGINATION", "IMAGINATIVE", "IMMACULATE", "IMMORTAL", "IMPARTIAL",
			"IMPARTIALITY", "IMPERATIVE", "IMPERVIOUS", "IMPETUS", "IMPORTANCE", "IMPORTANT", "IMPRESS#1", "IMPRESSIVE",
			"IMPROVE#1", "IMPROVE#2", "IMPROVE#3", "IMPROVEMENT", "IMPUNITY", "INAUGURATE", "INAUGURATION",
			"INDEPENDENCE", "INDEPENDENT#1", "INDEPENDENT#2", "INDESCRIBABLE", "INDICATIVE", "INDISPENSABILITY",
			"INDISPENSABLE", "INDIVIDUALITY", "INDOMITABLE", "INDULGENCE", "INDUSTRIOUS", "INEXPENSIVE",
			"INFALLIBILITY", "INFALLIBLE", "INFER", "INFERENCE", "INFORM#1", "INFORM#2", "INGENIOUS", "INGENUITY",
			"INHERIT", "INNOCENCE", "INNOCENT", "INNOVATE", "INNOVATIVE", "INQUISITIVE", "INSEPARABLE", "INSIGHT",
			"INSISTENT", "INSPIRATION", "INSPIRATIONAL", "INSPIRE#1", "INSPIRE#2", "INSTINCTIVE", "INTEGRITY",
			"INTELLECT", "INTELLECTUAL#1", "INTELLECTUAL#2", "INTELLECTUAL#3", "INTELLIGENCE", "INTELLIGENT",
			"INTELLIGIBLE", "INTERCEDE", "INTERCOURSE", "INTEREST#1", "INTEREST#3", "INTEREST#4", "INTERESTED#1",
			"INTERESTED#2", "INTERPOSE", "INTIMACY", "INTIMATE", "INTRICATE", "INTRIGUE", "INVALUABLE", "INVENTOR",
			"INVINCIBLE", "INVITATION", "INVITE#1", "INVITE#2", "INVULNERABLE", "IRRESISTIBLE", "JEST", "JOIN#2",
			"JOINTLY", "JOKE#1", "JOKE#2", "JOKE#3", "JOLLY", "JOY", "JOYFUL", "JUBILANT", "JUBILEE", "JUST#3",
			"JUST#4", "JUSTICE", "JUSTIFIABLY", "JUSTIFICATION", "JUSTIFY", "KEEN", "KID#2", "KIND#2", "KIND#5",
			"KINDNESS", "KISS#1", "KISS#2", "KNOW#3", "KNOW#4", "KNOWLEDGE", "LAUDABLE", "LAUGH#1", "LAUGH#2",
			"LAUGH#5", "LAUGH#6", "LAUGHTER", "LAVISH", "LAW", "LAWFUL", "LEAD#4", "LEARN#2", "LEARN#3", "LEARNER",
			"LEGAL", "LEGITIMACY", "LEGITIMATE", "LEISURE", "LIBERAL#2", "LIBERAL#3", "LIBERALISM", "LIBERATE",
			"LIBERTY", "LIFELONG", "LIGHT#1", "LIGHT#3", "LIKABLE", "LIKE#2", "LIKE#3", "LIVE#6", "LIVELY", "LOGIC",
			"LOGICAL", "LONGEVITY", "LOVE#1", "LOVE#2", "LOVE#3", "LOVE#4", "LOVELINESS", "LOVELY", "LOVER#1",
			"LOVER#2", "LOYAL", "LOYALTY", "LUCID", "LUCK", "LUCKILY", "LUCKY", "LUCRATIVE", "LUMINOUS", "LUST",
			"LUSTER", "LUSTROUS", "LUXURIANT", "LUXURIOUS", "LUXURY", "LYRIC", "LYRICAL", "MAGICAL", "MAGNETIC",
			"MAGNIFICENCE", "MAGNIFICENT", "MAIN#1", "MAJESTIC", "MAJESTY", "MAJOR#1", "MAKE#6", "MAKE#7", "MANAGEABLE",
			"MANLY", "MARITAL", "MARRIAGE", "MARRY#1", "MARRY#2", "MARVEL", "MARVELOUS", "MASTERFUL", "MASTERY",
			"MATCHLESS", "MATE#1", "MATE#2", "MATTER#4", "MATURE#1", "MATURE#2", "MATURE#3", "MATURITY", "MAXIMIZE",
			"MEANINGFUL", "MEASURABLE", "MEDIATE", "MEDITATION", "MEET#5", "MELLOW", "MELODY", "MEMORABLE", "MEND",
			"MENTOR", "MERCIFUL", "MERCY", "MERIT#1", "MERIT#2", "MERITORIOUS", "MERRILY", "MERRIMENT", "MERRY", "MESH",
			"METICULOUS", "MIGHTY", "MILD", "MIND#_10", "MINDFUL", "MINISTER#4", "MINT", "MIRACLE", "MIRACULOUS",
			"MIRTH", "MOBILITY", "MOBILIZE", "MODERATE", "MODERATION", "MODERNITY", "MODEST", "MODESTY", "MOMENTOUS",
			"MONUMENTAL", "MORAL", "MORALE", "MORALISTIC", "MORALITY", "MOTIVATE", "MOTIVATED", "MOTIVATION", "MOTIVE",
			"MULTITUDE", "MUTUAL#1", "MUTUAL#2", "MYRIAD", "NATURAL#1", "NATURAL#2", "NAVIGABLE", "NEAT", "NECESSARILY",
			"NEGOTIATE", "NICE#1", "NICE#2", "NICE#3", "NICE#4", "NICHE", "NIMBLE", "NOBILITY", "NOBLE", "NOBLEMAN",
			"NOISELESS", "NOMINATE", "NON-VIOLENCE", "NON-VIOLENT", "NORMAL", "NOTABLE", "NOTORIETY", "NOURISH",
			"NOURISHMENT", "NOVELTY", "NURSE#2", "NURTURE", "NUTRIENT", "OASIS", "OBEDIENCE", "OBEDIENT", "OBEY",
			"OBJECTIVE#2", "OBJECTIVE#3", "OBLIGE", "OBTAIN", "OBTAINABLE", "OFFER#1", "OFFER#2", "OFFER#3", "OFFSET",
			"ONSET", "ONWARD", "OPEN#1", "OPEN#2", "OPPORTUNE", "OPPORTUNITY", "OPTIMAL", "OPTIMISM", "OPTIMISTIC",
			"OPTIONAL", "ORDER#2", "ORDER#5", "ORDER#6", "ORGANIZE#1", "ORGANIZE#2", "ORIGINALITY", "OUTGOING",
			"OUTLIVE", "OUTRIGHT", "OUTRUN", "OUTSET", "OUTSTANDING", "OUTWIT", "OVERCOME", "OVERJOYED", "PAINSTAKING",
			"PALATABLE", "PALATIAL", "PAMPER", "PARADISE", "PARAMOUNT", "PARDON#1", "PARDON#2", "PARTICULAR#1",
			"PARTNER", "PARTNERSHIP", "PASS#2", "PASSIONATE", "PATIENCE", "PATIENT#2", "PATIENT#3", "PATRIOT",
			"PATRIOTIC", "PATRON", "PATRONAGE", "PAY#4", "PEACE#1", "PEACE#2", "PEACEABLE", "PEACEFUL", "PEERLESS",
			"PERFECT#1", "PERFECT#2", "PERFECT#3", "PERFECTION", "PERFECTIONISM", "PERFECTIONIST", "PERFUME",
			"PERMISSION", "PERMIT#1", "PERSEVERANCE", "PERSEVERE", "PERSUASIVE", "PERTINENT", "PICTURESQUE", "PIETY",
			"PINNACLE", "PIOUS", "PLAIN#1", "PLAIN#3", "PLAIN#4", "PLAIN#5", "PLAUSIBILITY", "PLAYFUL", "PLAYMATE",
			"PLAYTHING", "PLEASANT#1", "PLEASANT#2", "PLEASANTRY", "PLEASE#2", "PLEASE#3", "PLEASED#1", "PLEASED#2",
			"PLEASURABLE", "PLEASURE", "PLEDGE", "PLENTIFUL", "PLENTY", "POETIC", "POIGNANT", "POISE#1", "POLISH",
			"POLITE", "POLITENESS", "POMP", "POPULAR", "POPULARITY", "POPULOUS", "PORTABLE", "POSITIVE", "POSITIVENESS",
			"POSITIVITY", "POSTERITY", "POTENCY", "POTENT", "PRACTICABLE", "PRACTICAL#1", "PRAISE#1", "PRAISE#2",
			"PRANCE", "PRECAUTION", "PRECEDENT", "PRECEPT", "PRECIOUS", "PRECISE", "PRECISION", "PREEMINENT",
			"PREFERENCE", "PREMIER", "PREMISE", "PREMIUM", "PREPARATORY", "PRESTIGE", "PRETTILY", "PRETTY#2",
			"PRETTY#3", "PRETTY#4", "PRETTY#5", "PRICELESS", "PRIDE", "PRIMARILY", "PRIME", "PRINCIPAL#1",
			"PRINCIPLE#1", "PRINCIPLE#2", "PRINCIPLE#4", "PRIVACY", "PRIVILEGED", "PRIVY", "PRIZE", "PRO", "PROACTIVE",
			"PRODIGIOUS", "PRODIGY", "PRODUCTIVE", "PRODUCTIVITY", "PROFESS", "PROFFER", "PROFICIENT", "PROFIT#1",
			"PROFIT#2", "PROFITABLE", "PROFOUND", "PROGRESS#1", "PROGRESS#2", "PROGRESSIVE", "PROLIFIC", "PROMINENCE",
			"PROMINENT", "PROMISE#3", "PROMISE#4", "PROMPT#1", "PROMPT#2", "PROMPTLY", "PROPER", "PROPITIOUS",
			"PROPRIETARY", "PROPRIETY", "PROSECUTE", "PROSPER", "PROSPERITY", "PROSPEROUS", "PROTECT", "PROTECTION",
			"PROTECTIVE", "PROTECTOR", "PROUD", "PROVIDE#1", "PROVIDENCE", "PROWESS", "PRUDENCE", "PRUDENT", "PUNCTUAL",
			"PURE", "PURIFICATION", "PURIFY", "PURITY", "PURPOSEFUL", "PURR", "QUAINT", "QUALIFY#1", "QUALIFY#3",
			"QUALITY#2", "QUENCH", "QUICKEN", "RADIANCE", "RADIANT", "RADIATE", "RALLY", "RAPPORT", "RAPT", "RAPTURE",
			"RATIONAL", "RAVE", "READILY", "REAL#1", "REALISTIC", "REALISTICALLY", "REAP", "REASONABLE", "REASSURANCE",
			"REASSURE", "RECEPTIVE", "RECLAIM", "RECLINE", "RECOMPENSE", "RECONCILE", "RECONCILIATION", "RECREATION",
			"REDEEM", "REDEMPTION", "REESTABLISH", "REFINE", "REFINEMENT", "REFUGE", "REGAL", "REGARD#3",
			"REHABILITATION", "REINFORCEMENT", "REINSTATE", "REJOICE", "RELAXATION", "RELEVANCE", "RELEVANCY",
			"RELEVANT", "RELIABILITY", "RELIABLE", "RELIEF", "RELIEVE#3", "RELIGIOUS#1", "RELISH", "REMARKABLE",
			"REMARKABLY", "REMEDY", "REMODEL", "RENAISSANCE", "RENEWAL", "RENOVATE", "RENOVATION", "RENOWN", "REPAIR#1",
			"REPAIR#2", "REPARATION", "REPENT", "REPENTANCE", "REPOSE", "REPUTABLE", "RESCUE#1", "RESCUE#2", "RESOLUTE",
			"RESOLVE#1", "RESOLVE#2", "RESOLVED", "RESOUND", "RESOURCEFUL", "RESOURCEFULNESS", "RESPECT#1", "RESPECT#2",
			"RESPECT#5", "RESPECTABLE", "RESPECTFUL", "RESPITE", "RESPLENDENT", "RESPONSIBILITY", "RESPONSIBLE#1",
			"RESPONSIVE", "RESTFUL", "RESTORATION", "RESTORE", "RESURRECT", "RETURN#2", "RETURN#5", "REUNION",
			"REUNITE", "REVEL", "REVELATION", "REVERE", "REVERENCE", "REVERENT", "REVERENTLY", "REVITALIZE", "REVIVAL",
			"REVIVE", "REWARD#1", "REWARD#2", "REWARD#3", "RICH#1", "RICH#2", "RICH#3", "RICH#4", "RICH#5", "RICH#6",
			"RICHES", "RICHNESS", "RIGHT#1", "RIGHT#6", "RIGHTEOUS", "RIGHTEOUSNESS", "RIGHTFUL", "RIPE", "RIPEN",
			"ROBUST", "ROMANCE", "ROMANTIC", "ROMANTICIZE", "ROSY", "ROUND#4", "ROUSE", "SACRED", "SAFE#1", "SAFE#2",
			"SAFE#3", "SAFE#4", "SAFEGUARD", "SAFETY#2", "SAGACITY", "SAGE", "SAINT", "SALUTARY", "SALUTATION",
			"SALUTE", "SALVATION", "SANCTIFY", "SANCTUARY", "SANE", "SANGUINE", "SANITARY", "SANITY", "SATISFACTION",
			"SATISFACTORILY", "SATISFACTORY", "SATISFY#1", "SATISFY#2", "SATISFY#3", "SAVE#1", "SAVINGS", "SAVOR",
			"SAVVY", "SCAMPER", "SCRUPLES", "SCRUPULOUS", "SECURE#2", "SECURE#3", "SECURITY#1", "SECURITY#2",
			"SELECTIVE", "SELF-CONTAINED", "SELF-RESPECT", "SEMBLANCE", "SENSATIONAL", "SENSE#3", "SENSE#5", "SENSIBLE",
			"SENSITIVE", "SENSITIVITY", "SERENE", "SERIOUS", "SERIOUSNESS", "SETTLE#4", "SHARE#1", "SHARE#3", "SHARE#4",
			"SHELTER#1", "SHELTER#2", "SHIELD", "SHINY", "SHREWD", "SHREWDNESS", "SIGNIFICANCE", "SIGNIFICANT",
			"SIGNIFY", "SIMPLICITY", "SIMPLIFY#1", "SIMPLIFY#2", "SINCERE", "SINCERITY", "SKILL#1", "SKILL#2",
			"SKILLFUL", "SLEEK", "SMART#1", "SMART#2", "SMART#4", "SMILE#1", "SMILE#2", "SMILE#3", "SMILE#4", "SMITTEN",
			"SOBER", "SOCIABLE", "SOFTNESS", "SOLACE", "SOLUTION", "SOOTHE", "SOPHISTICATED", "SOUGHT", "SOUND#4",
			"SOUND#6", "SOUNDNESS", "SPARE#2", "SPARKLE", "SPECIAL", "SPECTACULAR", "SPEEDILY", "SPLENDID", "SPLENDOR",
			"SPOTLESS", "SPRIGHTLY", "SQUARELY", "STABILITY", "STABILIZE", "STABLE", "STAND#5", "STANDARDIZE", "STAPLE",
			"STATELY", "STATUESQUE", "STAUNCH", "STAUNCHNESS", "STEADFAST", "STEADFASTNESS", "STEADINESS", "STEADY",
			"STIMULATE", "STIMULATION", "STOOD#4", "STRAIGHT#3", "STRAIGHTFORWARD", "STRUT", "STUD", "STUDIOUS",
			"STUPENDOUS", "STURDY", "STYLISH", "SUAVE", "SUBLIME", "SUBSCRIBE", "SUBSCRIPTION", "SUBSIDIZE", "SUBSIDY",
			"SUBSIST", "SUBSISTENCE", "SUBSTANTIATE", "SUBTLE", "SUCCEED#1", "SUCCESS", "SUCCESSFUL", "SUFFICE",
			"SUFFICIENT", "SUIT#3", "SUIT#4", "SUITABLE", "SUMMIT", "SUMPTUOUS", "SUPER", "SUPERIOR", "SUPERIORITY",
			"SUPERLATIVE", "SUPPORT#1", "SUPPORT#2", "SUPPORT#3", "SUPPORT#4", "SUPPORTIVE", "SUPREME#1", "SUPREME#2",
			"SURGE", "SURMISE", "SURMOUNT", "SURPASS", "SURVIVAL", "SURVIVE", "SURVIVOR", "SWEET#1", "SWEET#3",
			"SWEET#4", "SWEET#5", "SWEETEN", "SWEETHEART", "SWEETNESS", "SWIFTNESS", "SWOON", "SWORN", "SYMBOLIZE",
			"SYMMETRY", "SYMPATHETIC", "SYMPATHIZE", "SYMPATHY", "SYNTHESIS", "TACT", "TACTICS", "TALENT", "TALENTED",
			"TASTE#2", "TEMPERANCE", "TEMPERATE", "TEMPT", "TENACIOUS", "TENACITY", "TENDERNESS", "TERRIFIC", "THANK#1",
			"THANK#2", "THANK#3", "THANKFUL", "THERAPEUTIC", "THOROUGH", "THOUGHTFUL", "THOUGHTFULNESS", "THRIFT",
			"THRIFTY", "THRILL", "THRIVE", "TINGLE#1", "TOGETHERNESS", "TOLERANCE", "TOLERANT", "TOLERATE",
			"TOLERATION", "TOPMOST", "TRADITION", "TRADITIONAL", "TRAIN#4", "TRANQUIL", "TRANQUILITY", "TRAVEL#4",
			"TREASURE#1", "TREASURE#2", "TREAT#2", "TREATISE", "TREATY", "TREMENDOUS#1", "TREMENDOUS#2", "TRIBUTE",
			"TRIUMPH#1", "TRIUMPH#2", "TRIUMPHAL", "TRIUMPHANT", "TROPHY", "TRUE#1", "TRUE#2", "TRUE#3", "TRUE#4",
			"TRUE#5", "TRUST#1", "TRUST#2", "TRUST#3", "TRUST#4", "TRUST#6", "TRUST#7", "TRUSTWORTHINESS",
			"TRUSTWORTHY", "TRUTH", "TRUTHFUL", "UNBOUND", "UNBROKEN", "UNCOMMON", "UNDERSTAND#1", "UNDERSTAND#2",
			"UNDERSTANDABLE", "UNDERSTOOD", "UNDOUBTED", "UNDOUBTEDLY", "UNFORGETTABLE", "UNHURRIED", "UNIMPEACHABLE",
			"UNIQUE", "UNITY", "UNLIMITED", "UNSELFISH", "UNTOUCHED", "UPBEAT", "UPFRONT", "UPGRADE", "UPHELD",
			"UPHOLD", "UPLIFT", "UPPERMOST", "UPRIGHT", "UPSIDE", "UPWARD", "USABLE", "USEFUL", "USEFULNESS",
			"UTILITARIAN", "UTILIZATION", "UTILIZE", "UTTERMOST", "VALIANT", "VALID", "VALIDITY", "VALOR", "VALUABLE",
			"VALUE#2", "VALUE#3", "VANQUISH", "VASTNESS", "VENERABLE", "VENERATE", "VERIFICATION", "VERIFY",
			"VERITABLE", "VERSATILE", "VERSATILITY", "VIABILITY", "VIABLE", "VICTORIOUS", "VIGILANCE", "VIGILANT",
			"VIRTUE", "VIRTUOUS", "VISIONARY", "VISUALIZATION", "VITALITY", "VIVACIOUS", "VIVID", "VOLUNTARY",
			"VOUCHSAFE", "WARM#1", "WARM#2", "WARM#3", "WARM#4", "WARM#5", "WARMHEARTED", "WARMTH", "WELCOME#1",
			"WELCOME#2", "WELCOME#3", "WELCOME#5", "WELFARE#1", "WELL#2", "WELL#4", "WHIMSICAL", "WHOLESOME", "WILLFUL",
			"WILLING", "WILLINGNESS", "WISDOM", "WISE#1", "WISE#2", "WISE#3", "WISE#4", "WISE#5", "WITTY", "WONDER#1",
			"WONDERFUL", "WONDROUS", "WOO", "WORKABLE", "WORKMANSHIP", "WORLD-FAMOUS", "WORTH#1", "WORTH#2", "WORTH#3",
			"WORTH#4", "WORTH-WHILE", "WORTHINESS", "WORTHY", "ZENITH", "ZEST" };
	String[] nWords = { "ABANDON", "ABANDONMENT", "ABATE", "ABDICATE", "ABHOR", "ABJECT", "ABNORMAL", "ABOLISH",
			"ABOMINABLE", "ABRASIVE", "ABRUPT", "ABSCOND", "ABSENCE", "ABSENT#1", "ABSENT-MINDED", "ABSENTEE", "ABSURD",
			"ABSURDITY", "ABUSE#1", "ABUSE#2", "ABYSS", "ACCIDENT", "ACCOST", "ACCURSED", "ACCUSATION", "ACCUSE#1",
			"ACCUSE#2", "ACHE", "ACRIMONIOUS", "ACRIMONY", "ADDICT", "ADDICTION", "ADMONISH", "ADMONITION",
			"ADULTERATE", "ADULTERATION", "ADULTERY", "ADVERSARY", "ADVERSE", "ADVERSITY", "AFFECTATION", "AFFLICT",
			"AFFLICTION", "AFRAID#2", "AGAINST", "AGGRAVATE", "AGGRAVATION", "AGGRESSION", "AGGRESSIVE",
			"AGGRESSIVENESS", "AGGRESSOR", "AGGRIEVE", "AGHAST", "AGITATE", "AGITATION", "AGITATOR", "AGONIZE", "AGONY",
			"AIL", "AILMENT", "AIMLESS", "ALARM#2", "ALARMING", "ALAS", "ALIBI", "ALIEN", "ALIENATE", "ALIENATION",
			"ALLEGATION", "ALLEGE", "ALOOF", "ALTERCATION", "AMBIGUITY", "AMBIGUOUS", "AMBIVALENT", "AMBUSH#1",
			"AMBUSH#2", "AMISS", "AMPUTATE", "ANARCHIST", "ANARCHY", "ANGER#1", "ANGER#2", "ANGRY", "ANGUISH",
			"ANIMOSITY", "ANNIHILATE", "ANNIHILATION", "ANNOY", "ANNOYANCE", "ANOMALOUS", "ANOMALY", "ANTAGONISM",
			"ANTAGONIST", "ANTAGONISTIC", "ANTAGONIZE", "ANTI-SOCIAL", "ANTIPATHY", "ANTIQUATED", "ANTITRUST",
			"ANXIETY", "ANXIOUS#2", "ANXIOUS#3", "ANXIOUSNESS", "APATHETIC", "APATHY", "APPALL#1", "APPALL#2",
			"APPREHENSION", "APPREHENSIVE", "ARBITRARY", "ARDUOUS", "ARGUE#1", "ARGUMENT#1", "ARREST#1", "ARREST#2",
			"ARROGANCE", "ARROGANT", "ARTIFICIAL", "ASHAMED", "ASSAIL", "ASSAILANT", "ASSASSIN", "ASSASSINATE",
			"ASSAULT#1", "ASSAULT#2", "ASTRAY", "ASUNDER", "ATROCIOUS", "ATROPHY", "ATTACK#1", "ATTACK#2", "ATTACK#3",
			"AUDACIOUS", "AUDACITY", "AUSTERE", "AUTOCRAT", "AUTOCRATIC", "AVARICE", "AVARICIOUS", "AVENGE", "AVERSION",
			"AVERT", "AVOID", "AVOIDANCE", "AWFUL#1", "AWKWARD", "AWKWARDNESS", "AX", "BABBLE", "BACKWARD",
			"BACKWARDNESS", "BAD", "BADLY", "BAFFLE", "BAFFLEMENT", "BAIL", "BAIT", "BALK", "BANAL", "BANDIT", "BANE",
			"BANISH#1", "BANISH#2", "BANISHMENT", "BANKRUPT", "BANKRUPTCY", "BAR#4", "BARBARIAN", "BARBAROUS", "BARREN",
			"BARRIER", "BASHFUL", "BASTARD", "BATTLE#1", "BATTLE#2", "BATTLEFIELD", "BEASTLY", "BEAT#2", "BEAT#3",
			"BEAT#4", "BEG", "BEGGAR", "BEHEAD", "BELATED", "BELIE", "BELITTLE", "BELLIGERENT", "BELT#2", "BEREAVE",
			"BEREAVEMENT", "BEREFT", "BERSERK", "BESEECH", "BESET", "BESIEGE", "BETRAY", "BETRAYAL", "BEWARE",
			"BEWILDER", "BEWILDERMENT", "BIT#2", "BITCHY", "BITE#1", "BITE#2", "BITE#3", "BITTER", "BITTERNESS",
			"BIZARRE", "BLACKMAIL", "BLAH", "BLAME#1", "BLAME#2", "BLAND", "BLAST#1", "BLAST#2", "BLATANT", "BLEAK",
			"BLEMISH", "BLIND#1", "BLIND#2", "BLIND#3", "BLIND#4", "BLINDNESS", "BLOCK#1", "BLOCK#2", "BLOCKHEAD",
			"BLOODSHED", "BLOODTHIRSTY", "BLOODY", "BLOW#1", "BLOW#3", "BLUNDER", "BLUNT", "BLUR", "BLURT", "BOARD#7",
			"BOAST", "BOASTFUL", "BOGUS", "BOISTEROUS", "BOLT", "BOMB#1", "BOMB#2", "BOMBARD", "BOMBARDMENT", "BONDAGE",
			"BOOT", "BORE#1", "BORE#5", "BORE#6", "BOREDOM", "BOTHER#1", "BOTHER#3", "BOTHERSOME", "BOUND#3", "BOUT",
			"BOX#2", "BRAG", "BRANDISH", "BRAT", "BRAVADO", "BRAWL", "BRAZEN", "BREACH", "BREAK#1", "BREAK#4",
			"BREAK#5", "BREAKDOWN", "BRIBE", "BRISTLE", "BRITTLE", "BROKE#3", "BROKE#5", "BROKE#6", "BROKE#7",
			"BROKEN-HEARTED", "BROOD", "BRUISE", "BRUSQUE", "BRUTALITY", "BRUTE", "BRUTISH", "BUCKLE", "BUG#2",
			"BULLET", "BUM", "BUNGLE", "BURDEN#1", "BURDEN#2", "BURDENSOME", "BURGLAR", "BURGLARY", "BURN#1", "BURN#2",
			"BURN#3", "BURN#4", "BURY", "BUSYBODY", "BUTCHERY", "CALAMITY", "CALLOUS", "CANCEL", "CANCELLATION",
			"CANCER", "CANNIBAL", "CANNON", "CAPITAL#2", "CAPITULATE", "CAPRICIOUS", "CAPSIZE", "CAPTIVE", "CAPTURE#1",
			"CAPTURE#2", "CAREEN", "CARELESS", "CARELESSNESS", "CASUALTY", "CATACLYSM", "CATASTROPHE", "CATCH#3",
			"CAVE#2", "CENSOR", "CENSORSHIP", "CENSURE", "CHAFE", "CHALLENGE#1", "CHALLENGE#2", "CHAOS", "CHAOTIC",
			"CHARGE#2", "CHARGE#3", "CHARGE#6", "CHARGE#7", "CHASE#1", "CHASE#2", "CHASTISE", "CHEAP", "CHEAPEN",
			"CHEATER", "CHIDE", "CHILDISH", "CHOKE#1", "CHOKE#2", "CHORE", "CHRONIC", "CIRCLE#3", "CLAMOR", "CLAMOROUS",
			"CLASH", "CLATTER", "CLIQUE", "CLOG", "CLOSE#2", "CLOSE#7", "CLUB#2", "CLUB#3", "CLUMSY", "CLUTTER",
			"COARSE", "COARSENESS", "COCKINESS", "COCKY", "COERCE", "COERCION", "COERCIVE", "COLD#1", "COLD#2",
			"COLD#3", "COLD#6", "COLLAPSE#1", "COLLAPSE#2", "COLLIDE", "COLLISION", "COLLUSION", "COLONY", "COMBAT#1",
			"COMBAT#2", "COMBATANT", "COMMISERATION", "COMMIT#1", "COMMONER", "COMMONPLACE", "COMMOTION", "COMPEL#1",
			"COMPEL#2", "COMPETE", "COMPETITION", "COMPETITIVE", "COMPETITOR", "COMPLAIN", "COMPLAINT", "COMPLEX",
			"COMPLEXITY", "COMPLICATE#1", "COMPLICATE#2", "COMPLICATION", "COMPLICITY", "COMPULSION", "CONCEAL",
			"CONCEIT", "CONCERN#1", "CONCERN#2", "CONDEMN#1", "CONDEMN#2", "CONDEMNATION", "CONDESCENDING",
			"CONDESCENSION", "CONFESS", "CONFESSION", "CONFINE#1", "CONFINE#2", "CONFINEMENT", "CONFISCATE",
			"CONFISCATION", "CONFLICT#1", "CONFLICT#2", "CONFLICT#3", "CONFLICT#4", "CONFOUND", "CONFRONT",
			"CONFRONTATION", "CONFUSE#1", "CONFUSE#2", "CONFUSE#3", "CONFUSE#4", "CONFUSION", "CONGESTED", "CONGESTION",
			"CONSPIRACY", "CONSPIRATOR", "CONSPIRE", "CONSTERNATION", "CONSTRAIN", "CONSTRAINT", "CONSUMPTIVE",
			"CONTAGIOUS", "CONTAMINATE", "CONTAMINATION", "CONTEMPT", "CONTEMPTIBLE", "CONTEMPTUOUS", "CONTEND",
			"CONTRADICT", "CONTRADICTION", "CONTRADICTORY", "CONTRARY", "CONTROVERSIAL", "CONTROVERSY", "CONVICT#1",
			"CONVICT#2", "COOL", "COOLNESS", "CORRODE", "CORROSION", "CORROSIVE", "CORRUPT", "CORRUPTION", "COST#1",
			"COST#2", "COSTLINESS", "COSTLY", "COUNTERACT", "COUNTERACTION", "COUNTERFEIT", "COVERT", "COVET", "COWARD",
			"CRAFTY", "CRAM", "CRAMP", "CRANKY", "CRASS", "CRAVE", "CRAWL#2", "CRAZE", "CRAZINESS", "CRAZY",
			"CREDULOUS", "CRIME", "CRIMINAL", "CRINGE", "CRIPPLE", "CRISIS", "CRITIC", "CRITICIZE", "CROAK", "CROOK",
			"CROOKED", "CROSS#3", "CROSS#6", "CROSS#7", "CRUDE", "CRUEL", "CRUELTY", "CRUMBLE", "CRUMPLE", "CRUSH#1",
			"CRUSH#2", "CRUSHING", "CULPABLE", "CULPRIT", "CUMBERSOME", "CURSE#1", "CURSE#2", "CURSORY", "CURT",
			"CURTAIL", "CUT#1", "CUT#2", "CYNICAL", "CYNICISM", "DAMAGE#1", "DAMAGE#2", "DAMN", "DAMNABLE", "DAMNED",
			"DANGER", "DANGEROUS", "DARK", "DARKEN", "DARKNESS", "DARN", "DAUNTING", "DAWDLE", "DAZE", "DEAD",
			"DEADLOCK", "DEADLY", "DEADWEIGHT", "DEAF", "DEAFNESS", "DEAL#6", "DEARTH", "DEATH", "DEBATABLE", "DEBTOR",
			"DECADENCE", "DECADENT", "DECAY#1", "DECAY#2", "DECEASE", "DECEIT", "DECEITFUL", "DECEIVE#1", "DECEIVE#2",
			"DECEPTION", "DECEPTIVE", "DECLINE#1", "DECLINE#2", "DECOMPOSE", "DECREASE#1", "DECREASE#2", "DEFAME",
			"DEFAULT", "DEFEAT#1", "DEFEAT#2", "DEFECT", "DEFECTIVE", "DEFENDANT", "DEFENSIVE", "DEFIANCE", "DEFIANT",
			"DEFICIENCY", "DEFICIENT", "DEFICIT", "DEFILE", "DEFY", "DEGENERATE", "DEGRADE", "DEJECTED", "DELAY#1",
			"DELAY#2", "DELINQUENCY", "DELINQUENT", "DELIRIUM", "DELUGE", "DELUSION", "DEMEAN", "DEMISE", "DEMOLISH",
			"DEMON", "DEMORALIZE", "DENIAL", "DENOUNCE", "DENT", "DENY", "DEPENDENT", "DEPLETE", "DEPLORABLE",
			"DEPLORE", "DEPOSE", "DEPRAVED", "DEPRECIATE", "DEPRECIATION", "DEPRESS#1", "DEPRESS#2", "DEPRESS#3",
			"DEPRESSION#1", "DEPRESSION#2", "DEPRIVE", "DERIDE", "DERISION", "DERISIVE", "DEROGATORY", "DESERT#2",
			"DESERT#4", "DESERTION", "DESIRE#3", "DESOLATE", "DESOLATION", "DESPAIR#1", "DESPAIR#2", "DESPERATE",
			"DESPERATION", "DESPICABLE", "DESPISE#1", "DESPISE#2", "DESTITUTE", "DESTROY", "DESTRUCTION", "DESTRUCTIVE",
			"DETACHMENT", "DETAIN", "DETER", "DETERRENT", "DETEST", "DETESTABLE", "DETRACT", "DETRIMENTAL", "DEVASTATE",
			"DEVASTATION", "DEVIATE", "DEVIATION", "DEVIL", "DEVILISH", "DEVIOUS", "DEVOID", "DIABOLIC", "DIABOLICAL",
			"DICTATE", "DICTATORIAL", "DIFFER", "DIFFICULT", "DIFFICULTY", "DILEMMA", "DIM#1", "DIM#2", "DIN", "DIRE",
			"DIRT", "DIRTY", "DISABLE", "DISADVANTAGE", "DISADVANTAGEOUS", "DISAGREEABLE", "DISAGREEMENT",
			"DISAPPOINT#1", "DISAPPOINT#2", "DISAPPOINT#3", "DISAPPOINT#4", "DISAPPOINTMENT", "DISAPPROVAL",
			"DISAPPROVE", "DISARM", "DISASTER", "DISASTROUS", "DISAVOW", "DISAVOWAL", "DISBELIEF", "DISCHARGE#1",
			"DISCHARGE#2", "DISCLAIM", "DISCOMFORT", "DISCONCERTED", "DISCONTENT", "DISCORD", "DISCORDANT",
			"DISCOURAGE#1", "DISCOURAGE#2", "DISCOURAGEMENT", "DISCREDIT", "DISCREPANT", "DISCRIMINATE", "DISDAIN",
			"DISEASE", "DISEASED", "DISGRACE", "DISGUISE", "DISGUST#1", "DISGUST#2", "DISGUST#3", "DISGUST#4",
			"DISGUST#5", "DISHEARTEN", "DISHONEST", "DISHONOR", "DISINGENUOUS", "DISINTEREST", "DISLIKE#1", "DISLIKE#2",
			"DISMAL", "DISMISS#1", "DISMISS#2", "DISOBEDIENCE", "DISOBEDIENT", "DISORDER", "DISORGANIZED", "DISPEL",
			"DISPENSABILITY", "DISPENSE", "DISPLACE", "DISPLEASE", "DISPLEASURE", "DISPOSAL", "DISPOSE#1", "DISPOSE#2",
			"DISPROPORTIONATE", "DISPROVE", "DISPUTABLE", "DISPUTE#1", "DISPUTE#2", "DISREGARD", "DISRUPT",
			"DISRUPTION", "DISSATISFACTION", "DISSATISFIED", "DISSATISFY", "DISSENT", "DISSENTION", "DISSOLUTION",
			"DISTORT", "DISTORTION", "DISTRACT", "DISTRACTING", "DISTRACTION", "DISTRESS#1", "DISTRESS#2",
			"DISTRUSTFUL", "DISTURB#1", "DISTURB#2", "DISTURB#3", "DISTURBANCE", "DIVERSION", "DIVERT", "DIVIDE#1",
			"DIVIDE#2", "DIVISION", "DIVORCE#1", "DIVORCE#2", "DIVORCE#3", "DIZZY", "DOLDRUMS", "DOMINATE#1",
			"DOMINATE#2", "DOMINATE#3", "DOMINATION", "DOOM#1", "DOOM#2", "DOOMSDAY", "DOPE", "DOUBLE#3", "DOUBLE#4",
			"DOUBT#1", "DOUBT#2", "DOUBT#4", "DOUBTFUL", "DOWNCAST", "DOWNFALL", "DOWNHEARTED", "DRAB", "DRAG", "DREAD",
			"DREADFUL", "DREARY", "DRIVE#2", "DROOP", "DROP#1", "DROP#3", "DROP#4", "DROUGHT", "DROWN#1", "DROWN#2",
			"DROWSINESS", "DROWSY", "DRUNK#1", "DRUNK#2", "DRUNKARD", "DRUNKEN", "DUBIOUS", "DULL", "DUMB", "DUMP#1",
			"DUMP#2", "DUNCE", "DUNGEON", "DUTY", "DWINDLE", "DYING#1", "DYING#2", "ECCENTRIC", "ECCENTRICITY",
			"EDGE#4", "EGOTISTICAL", "ELIMINATE", "ELIMINATION", "EMBARRASS", "EMBARRASSMENT", "EMERGENCY", "EMPTY#1",
			"EMPTY#2", "ENCROACH", "ENCROACHMENT", "ENDANGER", "ENEMY", "ENFORCE", "ENGULF", "ENRAGE", "ENSLAVE",
			"ENTANGLE", "ENTANGLEMENT", "ENTREAT", "ENVIOUS", "ENVY", "EPIDEMIC", "EPITHET", "EQUIVOCAL", "ERADICATE",
			"ERASE", "ERODE", "EROSION", "ERR", "ERRONEOUS", "ERROR", "ESOTERIC", "ESTRANGED", "EVADE", "EVASION",
			"EVEN#3", "EVICT", "EVIL#1", "EVIL#2", "EXAGGERATION", "EXASPERATE", "EXASPERATION", "EXCEPTION#2",
			"EXCESS", "EXCESSIVE", "EXCLUDE", "EXCLUSION", "EXCOMMUNICATION", "EXECUTE", "EXEMPT", "EXHAUST",
			"EXHAUSTION", "EXILE", "EXIT", "EXPEDIENT", "EXPEL", "EXPENSE#1", "EXPENSE#2", "EXPENSIVE", "EXPLODE",
			"EXPLOIT#2", "EXPLOSION", "EXPLOSIVE", "EXPOSE", "EXTERMINATE", "EXTERMINATION", "EXTINCT", "EXTINGUISH",
			"EXTRAVAGANT", "FABRICATE", "FABRICATION", "FAIL#1", "FAIL#2", "FAIL#3", "FAIL#4", "FAILURE", "FAINT",
			"FAKE", "FALL#7", "FALLACY", "FALLOUT", "0", "FALSEHOOD", "FALTER", "FAMINE", "FAMISHED", "FANATIC",
			"FANATICAL", "FARCE", "FASCIST", "FAT#1", "FAT#2", "FAT#3", "FATAL", "FATALISTIC", "FATIGUE", "FAULT",
			"FEAR#1", "FEAR#2", "FEAR#3", "FEARFUL", "FEARSOME", "FED#2", "FEEBLE", "FEIGN", "FEINT", "FEROCIOUS",
			"FEROCITY", "FEUD", "FEUDAL", "FEVER", "FEVERISH", "FIASCO", "FICKLE", "FIDGET", "FIEND", "FIERCE",
			"FIGHT#1", "FIGHT#2", "FIGHT#3", "FIGHTER", "FILTH", "FILTHY", "FINE#6", "FINE#7", "FIRE#1", "FIRE#2",
			"FIRE#3", "FIST", "FIX#5", "FLAGRANT", "FLAW", "FLED", "FLEE", "FLEETING", "FLIMSY", "FLOOR#5", "FLOUNDER",
			"FOE", "FOIBLE", "FOOL#1", "FOOL#2", "FOOLISH", "FOOLISHNESS", "FORBID", "FORBIDDEN", "FORCE#3", "FORCE#5",
			"FORCE#6", "FOREBODING", "FOREIGN#2", "FORFEIT", "FORGETFULNESS", "FORLORN", "FORMIDABLE", "FORSAKE",
			"FOUGHT", "FOUNDER#2", "FRACTURE", "FRAGILE", "FRANTIC", "FRANTICALLY", "FRAUD", "FRAUDULENT", "FRAUGHT",
			"FREAK", "FRET", "FRETFUL", "FRIGHTEN#1", "FRIGHTEN#2", "FRIGHTEN#3", "FRIGHTFUL", "FRIGID", "FRIVOLOUS",
			"FRONT#3", "FROWN#1", "FROWN#2", "FRUITLESS", "FRUSTRATE#1", "FRUSTRATE#2", "FRUSTRATE#3", "FRUSTRATION",
			"FUGITIVE", "FUMBLE", "FUN#2", "FURIOUS", "FURY", "FUSS", "FUSSY", "FUTILITY", "GALL", "GAMBLE#1",
			"GAMBLE#2", "GASH", "GAUDY", "GERM", "GET#5", "GHASTLY", "GHETTO", "GLARE", "GLOAT", "GLOOM", "GLOOMY",
			"GLUM", "GODDAMN", "GRAB", "GRAPPLE", "GRATUITOUS", "GRAVE#1", "GRAVE#2", "GRAVE#3", "GRAVE#4", "GRAVE#5",
			"GRIEF", "GRIEVANCE", "GRIEVE", "GRIM", "GRIZZLY", "GROTESQUE", "GROWL", "GRUDGE", "GRUFF", "GRUMBLE",
			"GUERRILLA", "GUILT", "GUILTY", "GUISE", "GULLIBLE", "GUN", "GUNMEN", "HACK", "HACKNEY", "HAG", "HAGGARD",
			"HAMPER", "HAND#6", "HANDICAP", "HANG#3", "HANG#6", "HAPHAZARD", "HAPLESS", "HARASS", "HARASSMENT",
			"HARD#1", "HARD#5", "HARDSHIP", "HARM#1", "HARM#2", "HARMFUL", "HARSH", "HASSLE", "HATE#1", "HATE#2",
			"HATE#3", "HATER", "HATRED", "HAUGHTY", "HAUNT", "HAVOC", "HAZARD#1", "HAZARD#2", "HAZARDOUS", "HAZINESS",
			"HAZY", "HEADACHE", "HEARTLESS", "HECTIC", "HEDGE", "HEDONISTIC", "HEEDLESS", "HEINOUS", "HELL", "HELP#3",
			"HELPLESS", "HELPLESSNESS", "HIDEOUS", "HINDER", "HINDRANCE", "HIT#1", "HIT#4", "HOARD", "HOBBLE", "HOLE#2",
			"HOLE#3", "HOLLOW", "HOMELY", "HOPELESS", "HORDE", "HORN#3", "HORRIBLE", "HORRID", "HORRIFY", "HORROR",
			"HOSTILE", "HOSTILITY", "HOT#6", "HUMILIATE", "HUMILIATION", "HUNG#3", "HUNGER", "HUNGRY", "HUNT#1",
			"HUNT#2", "HUNT#3", "HUNTER", "HURT#1", "HURT#2", "HURTFUL", "HUSTLE", "HUSTLER", "HYPOCRISY", "HYPOCRITE",
			"HYSTERIA", "HYSTERICAL", "IDIOT", "IDIOTIC", "IDLENESS", "IGNOBLE", "IGNORANCE", "IGNORANT", "ILL#1",
			"ILL#2", "ILL#3", "ILL#4", "ILLEGAL", "ILLEGALITY", "ILLITERATE", "ILLNESS", "ILLOGICAL", "IMMATURE",
			"IMMOBILITY", "IMMORAL", "IMMORALITY", "IMMOVABLE", "IMPAIR", "IMPASSE", "IMPATIENCE", "IMPATIENT",
			"IMPEDE", "IMPEDIMENT", "IMPERFECT", "IMPERSONAL", "IMPETUOUS", "IMPLICATE", "IMPLORE", "IMPOSE",
			"IMPRECISION", "IMPRISON", "IMPRISONMENT", "IMPROPER", "IMPULSIVE", "IMPURE", "IMPURITY", "INABILITY",
			"INACCESSIBLE", "INACCURACY", "INADEQUATE", "INANE", "INCAPABLE", "INCESSANT", "INCOMPATIBILITY",
			"INCOMPATIBLE", "INCOMPETENCE", "INCOMPETENT", "INCONSISTENCY", "INCONVENIENT", "INCORRECT", "INCREDIBLE",
			"INCURABLE", "INDECENT", "INDEFINITE", "INDETERMINABLE", "INDETERMINATE", "INDICTMENT", "INDIFFERENCE",
			"INDIFFERENT", "INDIGNATION", "INDULGE", "INEFFECTIVE", "INEFFECTIVENESS", "INEFFECTUAL", "INEFFECTUALNESS",
			"INEFFICIENCY", "INEQUALITY", "INEXACT", "INEXPLICABLE", "INFAMOUS", "INFECT", "INFECTION", "INFERIOR",
			"INFERIORITY", "INFEST", "INFILTRATION", "INFLAME", "INFLATION", "INFLICT", "INFRACTION", "INFRINGEMENT",
			"INFURIATE", "INGRATITUDE", "INHIBIT", "INHIBITION", "INHUMANE", "INJUNCTION", "INJURE", "INJURIOUS",
			"INJURY", "INSANE", "INSECURE", "INSECURITY", "INSENSIBLE", "INSIDIOUS", "INSIGNIFICANT", "INSINUATE",
			"INSOLENCE", "INSOLENT", "INSTABILITY", "INSTABLE", "INSUFFICIENCY", "INSUFFICIENT", "INSULT", "INTERFERE",
			"INTERFERENCE", "INTERRUPT", "INTERRUPTION", "INTERVENTION", "INTIMIDATE", "INTOLERABLE", "INTOXICATE",
			"INTRUDE", "INTRUDER", "INTRUSION", "INUNDATE", "INUNDATED", "INVADE", "INVALID", "INVISIBLE",
			"INVOLUNTARY", "INVOLVE#5", "IRK", "IRON#3", "IRON#5", "IRONIC", "IRONY", "IRRATIONAL", "IRREGULAR",
			"IRREGULARITY", "IRRESPONSIBLE", "IRRITABLE", "IRRITATION", "ISOLATE#1", "ISOLATE#2", "JAIL#1", "JAIL#2",
			"JAR#2", "JEER", "JEOPARDIZE", "JEOPARDY", "JERK#1", "JERK#2", "JITTERY", "JOBLESS", "JUMPY", "JUNK",
			"KICK#1", "KICK#3", "KICK#4", "KIDNAP", "KILL#1", "KILL#2", "KILLER", "KNIFE#2", "KNOCK#1", "KNOCK#2",
			"KNOCK#3", "LACK#1", "LACK#2", "LACK#3", "LAG", "LAID#2", "LAME", "LAMENT", "LAMENTABLE", "LANGUISH",
			"LAPSE", "LAUGH#3", "LAWLESS", "LAY#3", "LAZILY", "LAZY", "LEAK", "LEAKAGE", "LET#3", "LIABILITY", "LIABLE",
			"LIAR", "LIE#2", "LIE#3", "LIFELESS", "LIMIT#2", "LIMIT#3", "LIMIT#4", "LIMITATION", "LIMP", "LIQUIDATE",
			"LIQUIDATION", "LITTER", "LOAD#3", "LONE#1", "LONELINESS", "LONELY", "LONER", "LONESOME", "LOOM", "LOSE#1",
			"LOSE#2", "LOSER", "LOSS", "LOST#1", "LOST#2", "LOST#3", "LOST#4", "LOVELESS", "LOW#1", "LOW#2", "LOWER#1",
			"LOWLY", "LUDICROUS", "LULL", "LUNATIC", "LURE", "LURK", "LYING#2", "LYING#3", "MAD#1", "MAD#2", "MADMAN",
			"MADNESS", "MAKE#2", "MALADJUSTED", "MALADJUSTMENT", "MALADY", "MALICE", "MALICIOUS", "MALIGNANT", "MANGLE",
			"MANIPULATE", "MANIPULATION", "MANSLAUGHTER", "MAR#1", "MAR#2", "MARGINAL", "MASSACRE", "MATTER#5",
			"MEAGER", "MEAN#2", "MEANINGLESS", "MEDDLE", "MEDIOCRE", "MEEK", "MELANCHOLY", "MELODRAMATIC", "MENACE",
			"MENIAL", "MERCILESS", "MESS#1", "MESS#2", "MIND#9", "MINE#3", "MISBEHAVE", "MISBEHAVIOR", "MISCHIEF",
			"MISCHIEVOUS", "MISER", "MISERABLE", "MISERY", "MISFORTUNE", "MISHANDLE", "MISHAP", "MISINFORM",
			"MISINFORMED", "MISLEAD", "MISREPRESENT", "MISS#1", "MISTAKE#1", "MISTAKE#2", "MISTAKE#3", "MISTAKEN",
			"MISTRUST", "MISUNDERSTAND", "MISUNDERSTANDING", "MISUNDERSTOOD", "MISUSE", "MIX#3", "MOAN", "MOCK",
			"MOCKERY", "MOLEST", "MONOTONOUS", "MONOTONY", "MONSTER", "MONSTROUS", "MOODY", "MORTIFY", "MOTIONLESS",
			"MOTLEY", "MOURN#1", "MOURN#2", "MOURNER", "MUDDLE", "MUDDY", "MUMBLE", "MUNDANE", "MURDER#1", "MURDER#2",
			"MURDEROUS", "MURKY", "MUTTER", "MYSELF>#3", "NAG#2", "NAIVE", "NASTY", "NAUGHTY", "NEBULOUS", "NEED#1",
			"NEED#2", "NEED#3", "NEEDLE#2", "NEEDY", "NEGATE", "NEGATION", "NEGATIVE", "NEGLECT#1", "NEGLECT#2",
			"NEGLIGENCE", "NEGLIGENT", "NERVOUS", "NERVOUSNESS", "NEUROTIC", "NEUTRALIZE", "NIGHTMARE", "NIX", "NOISE",
			"NONCHALANT", "NONSENSE", "NOSEY", "NOTORIOUS", "NOVICE", "NUISANCE", "NULLIFICATION", "NULLIFY", "NUMB",
			"NUTS", "OBJECT#3", "OBJECTION", "OBLIQUE", "OBLITERATE", "OBNOXIOUS", "OBSCURE", "OBSOLETE", "OBSTACLE",
			"OBSTINATE", "OBSTRUCT", "OBSTRUCTION", "ODD", "ODDITY", "OFFEND", "OFFENDER", "OFFENSIVE", "OMINOUS",
			"OMISSION", "OMIT", "OPINIONATED", "OPPONENT", "OPPOSE#1", "OPPOSE#2", "OPPOSE#3", "OPPOSITION", "OPPRESS",
			"OPPRESSION", "OPPRESSIVE", "ORDEAL", "ORDER#8", "ORPHAN", "OSTRACIZE", "OUST", "OUTBREAK", "OUTBURST",
			"OUTCAST", "OUTCRY", "OUTLAW", "OUTRAGE", "OUTRAGEOUS", "OUTSIDER", "OVERBEARING", "OVERFLOW", "OVERLOOK",
			"OVERPOWER", "OVERRUN", "OVERSIGHT", "OVERTHROW", "OVERTURN", "OVERWHELMING", "OVERWORKED", "OWE", "PAIN",
			"PAINFUL", "PALTRY", "PANDEMONIUM", "PANIC", "PARALYSIS", "PARALYZED", "PARANOID", "PARASITE",
			"PARTICULAR#2", "PARTITION", "PASS#8", "PASS#_10", "PASS#_13", "PASSE", "PATHETIC", "PATRONIZE", "PECULIAR",
			"PERIL", "PERILOUS", "PERISH", "PERPLEX", "PERPLEXITY", "PERSECUTE", "PERSECUTION", "PERTURB", "PERVERSE",
			"PERVERT", "PESSIMISM", "PESSIMISTIC", "PEST", "PETTY", "PHOBIA", "PICK#3", "PIECE#4", "PINCH#1", "PINCH#2",
			"PITIFUL", "PITILESS", "PLAGUE#1", "PLAGUE#2", "PLAINTIFF", "PLIGHT", "PLOD", "PLOT#2", "POINT#6",
			"POINTLESS", "POISON", "POISONOUS", "POLLUTE", "POLLUTION", "POMPOUS", "POOR#1", "POOR#2", "POOR#3",
			"POOR#4", "POOR#5", "POOR#6", "POUND#2", "POUT", "POVERTY", "POWERLESS", "PRECARIOUS", "PRECIPITATE",
			"PREDICAMENT", "PREJUDICE", "PREJUDICIAL", "PREPOSTEROUS", "PRESS#4", "PRESUMPTUOUS", "PRETEND", "PRETENSE",
			"PRETENTIOUS", "PRISON#1", "PRISON#2", "PRISONER", "PROBLEM", "PROCRASTINATE", "PROCRASTINATION", "PROD",
			"PROHIBIT", "PROHIBITION", "PROHIBITIVE", "PROPAGANDA", "PROSECUTION", "PROTEST#1", "PROTEST#2",
			"PROVOCATION", "PROVOKE", "PROWL", "PRY", "PUNCH", "PUNISH", "PUNY", "PUSH#1", "PUSH#2", "PUSH#3",
			"PUZZLEMENT", "QUALM", "QUANDARY", "QUARREL#1", "QUARREL#2", "QUARRELSOME", "QUEER", "QUESTIONABLE",
			"QUIBBLE", "QUIT", "QUITTER", "RACE#4", "RADICAL", "RAGE", "RAID", "RAISE#5", "RAMBLE", "RAMPANT", "RASCAL",
			"RASH", "RAT", "RATION", "RATTLE#2", "RAVAGE", "REACTIONARY", "REACTIVE", "REBEL#1", "REBEL#2", "REBELLION",
			"REBELLIOUS", "REBUFF", "REBUKE", "REBUT", "RECALCITRANT", "RECEDE", "RECESSION", "RECKLESS",
			"RECKLESSNESS", "RECOIL", "REDUNDANCY", "REDUNDANT", "REFRAIN", "REFUGEE", "REFUSAL", "REFUSE#1",
			"REGARDLESS", "REGRESS", "REGRESSION", "REGRET#1", "REGRET#2", "REGRETTABLE", "REJECT", "REJECTION",
			"RELAPSE", "RELUCTANT", "REMORSE", "RENOUNCE", "RENUNCIATION", "REPEAL", "REPREHENSIBLE", "REPRESS",
			"REPROACH", "REPUDIATE", "REPUGNANT", "REPULSE", "REPULSIVE", "RESENT", "RESENTFUL", "RESENTMENT",
			"RESIGNATION", "RESTLESS", "RESTLESSNESS", "RESTRICT#1", "RESTRICT#2", "RESTRICT#3", "RESTRICTION",
			"RETALIATE", "RETARD", "RETIRE", "RETREAT#1", "RETREAT#2", "REVENGE", "REVERT", "REVOKE", "REVOLT",
			"REVOLUTION", "RID", "RIDE#3", "RIDICULE#1", "RIDICULE#2", "RIDICULOUS", "RIGID", "RIGOR", "RIGOROUS",
			"RIP#1", "RIP#2", "RISKY", "RIVAL#1", "RIVAL#2", "RIVALRY", "ROBBER", "ROBBERY", "ROGUE", "ROOT#4", "ROT",
			"ROTTEN", "ROUGH#1", "ROUGHNESS", "ROUNDABOUT", "RUBBISH", "RUDE", "RUE", "RUFFIAN", "RUIN#1", "RUIN#2",
			"RUIN#3", "RUIN#4", "RUINOUS", "RUMOR", "RUMPLE", "RUN#4", "RUN#5", "RUNAWAY", "RUPTURE#1", "RUPTURE#2",
			"RUSTY", "RUTHLESS", "RUTHLESSNESS", "SABOTAGE", "SAD", "SADNESS", "SAG", "SANK", "SAP", "SARCASM",
			"SARCASTIC", "SAVAGE", "SCALD", "SCANDAL", "SCANDALOUS", "SCAPEGOAT", "SCAR", "SCARCITY", "SCARE#1",
			"SCARE#2", "SCARED#1", "SCARED#2", "SCARY", "SCHEME#1", "SCHEME#2", "SCOFF", "SCOLD#1", "SCOLD#2", "SCORCH",
			"SCORN", "SCORNFUL", "SCOUNDREL", "SCOWL", "SCRAPE", "SCREAM#1", "SCREAM#2", "SCREECH#1", "SCREECH#2",
			"SCREW#2", "SCRUTINIZE", "SCUFFLE", "SCUM", "SECEDE", "SECESSION", "SECRECY", "SECRET", "SEDENTARY",
			"SEETHE", "SEGREGATION", "SEIZE", "SELFISH", "SELFISHNESS", "SENILE", "SENSELESS", "SENTENCE#2",
			"SEQUESTER", "SERVE#4", "SERVICE#3", "SERVITUDE", "SEVER", "SEVERE", "SEVERITY", "SHABBY", "SHADOW#2",
			"SHADY", "SHAGGY", "SHAKE#1", "SHALLOW", "SHAME", "SHAMEFUL", "SHAMELESS", "SHARK", "SHELL#2", "SHELL#3",
			"SHIPWRECK", "SHIRK", "SHIVER", "SHOCK#1", "SHOCK#2", "SHOCK#3", "SHOCK#4", "SHOCK#5", "SHODDY", "SHOOT#1",
			"SHOOT#3", "SHOOT#6", "SHORT#5", "SHORTAGE", "SHORTCOMING", "SHORTSIGHTED", "SHOT#1", "SHOVE", "SHOW#4",
			"SHRED", "SHREW", "SHRIEK", "SHRILL", "SHRIVEL", "SHROUD", "SHRUG#1", "SHRUG#2", "SHUN", "SHYNESS",
			"SICK#1", "SICK#2", "SICK#3", "SICK#4", "SICKLY", "SICKNESS", "SIEGE", "SILLY", "SIMPLISTIC", "SIN#1",
			"SIN#2", "SINFUL", "SINISTER", "SKEPTICAL", "SKETCHY", "SKIRMISH", "SKULK", "SLAM", "SLANDER", "SLANDERER",
			"SLANDEROUS", "SLAP", "SLASH", "SLAUGHTER", "SLAYER", "SLEAZY", "SLEEPLESS", "SLIGHT#2", "SLIGHT#3",
			"SLIGHT#4", "SLIGHT#5", "SLIME", "SLOPPY", "SLOTH", "SLOTHFUL", "SLUG", "SLUGGISH", "SLUMP", "SLY", "SMACK",
			"SMASH#1", "SMASH#2", "SMEAR", "SMOTHER", "SMUGGLE", "SNARE", "SNARL", "SNATCH", "SNEAK", "SNEER", "SNORE",
			"SOB", "SOMBER", "SORE", "SORENESS", "SORROW", "SORROWFUL", "SORRY#1", "SORRY#2", "SORT#3", "SOUR", "SPANK",
			"SPEAR#2", "SPILL", "SPINSTER", "SPITE#2", "SPITEFUL", "SPLIT#3", "SPOIL", "SPOT#5", "SPRAIN", "SPUTTER",
			"SQUANDER", "STAB", "STAGNANT", "STAIN", "STALE", "STALEMATE", "STAMMER", "STAMP#2", "STANDSTILL", "STARK",
			"STARTLE", "STARVATION", "STARVE", "STATIC", "STEAL#1", "STEAL#2", "STERN", "STICK#6", "STIFLE", "STIGMA",
			"STING", "STINK", "STOLE", "STOLEN#1", "STOLEN#2", "STORM#2", "STORMY", "STRAGGLE", "STRAGGLER", "STRAIN#1",
			"STRAIN#2", "STRANGLE", "STRAY", "STRESS#1", "STRESS#2", "STRICKEN", "STRICT#1", "STRICT#2", "STRICT#3",
			"STRIFE", "STRIKE#1", "STRIKE#5", "STRINGENT", "STRUCK#1", "STRUGGLE#1", "STRUGGLE#2", "STRUGGLE#3",
			"STUBBORN", "STUBBORNLY", "STUBBORNNESS", "STUDY#3", "STUFFY", "STUNT", "STUPID#1", "STUPID#2", "STUPID#3",
			"STUPIDITY", "STUPOR", "SUBJECTION", "SUBJUGATE", "SUBJUGATION", "SUBMISSIVE", "SUBSERVIENCE", "SUBSIDE",
			"SUBSTITUTION", "SUBTRACT", "SUBVERSION", "SUBVERT", "SUCCUMB", "SUCKER", "SUFFER#1", "SUFFER#3",
			"SUFFERER", "SUFFOCATE", "SULLEN", "SUNDER", "SUPERFICIAL", "SUPERFICIALITY", "SUPERFLUOUS", "SUPERSTITION",
			"SUPERSTITIOUS", "SUPPRESS", "SUPPRESSION", "SUSCEPTIBLE", "SUSPECT#1", "SUSPECT#2", "SUSPEND",
			"SUSPENSION", "SUSPICION", "SUSPICIOUS", "SWEAR", "SWORE", "SYMPTOM", "TABOO", "TAINT", "TAMPER", "TANTRUM",
			"TARDY", "TARIFF", "TATTER", "TAUNT#1", "TAUNT#2", "TAX#2", "TEAR#1", "TEAR#3", "TEASE", "TEDIOUS",
			"TEMPER#1", "TEMPER#2", "TEMPEST", "TEMPORARILY", "TEMPTATION", "TENSE", "TENSION", "TERRIBLE", "TERRIFY",
			"TERROR", "TERRORISM", "TERRORIZE", "THEFT", "THIEF", "THIRST", "THIRSTY", "THORNY", "THOUGHTLESS",
			"THRASH", "THREAT", "THREATEN", "THROW#2", "THROW#3", "THUD", "THWART", "TIMIDITY", "TIRE#2", "TIRE#3",
			"TIRE#4", "TIRED#1", "TIRED#2", "TIRED#3", "TIRED#4", "TIRESOME", "TOIL", "TOLERABLE", "TOO#3", "TOPPLE",
			"TORMENT", "TORRENT", "TORTUROUS", "TOUCHY", "TRAGEDY", "TRAGIC", "TRAITOR", "TRAMP", "TRAMPLE",
			"TRANSGRESS", "TRAP#1", "TRAP#2", "TRAUMA", "TRAUMATIC", "TREACHEROUS", "TREACHERY", "TREASON",
			"TREASONOUS", "TRESPASS", "TRICK#1", "TRICK#2", "TRIVIAL", "TROUBLE#1", "TROUBLE#2", "TROUBLE#3",
			"TROUBLE#4", "TROUBLESOME", "TRUANT", "TRUDGE", "TRY#4", "TRY#5", "TURBULENT", "TURMOIL", "TURN#5",
			"TURN#8", "TWITCH", "TYRANNY", "UGLY", "ULTIMATUM", "UNACCUSTOMED", "UNARM", "UNATTRACTIVE", "UNAUTHENTIC",
			"UNAVOIDABLE", "UNBEARABLE", "UNBELIEVABLE", "UNCIVIL", "UNCLEAN", "UNCLEAR", "UNCOMFORTABLE", "UNCOUTH",
			"UNDEPENDABILITY", "UNDEPENDABLE", "UNDERMINE", "UNDERWORLD", "UNDESIRABLE", "UNDID", "UNDIGNIFIED", "UNDO",
			"UNDONE", "UNDUE", "UNEASINESS", "UNEASY", "UNECONOMICAL", "UNEMPLOYED", "UNEQUAL", "UNEVEN",
			"UNEXPECTEDLY", "UNFAIR", "UNFAITHFUL", "UNFAMILIAR", "UNFAVORABLE", "UNFEELING", "UNFIT", "UNFORESEEN",
			"UNFORTUNATE#1", "UNFORTUNATE#2", "UNFORTUNATE#3", "UNFRIENDLY", "UNGRATEFUL", "UNGUARDED", "UNHAPPINESS",
			"UNHAPPY", "UNHEALTHY", "UNIMPORTANT", "UNINFORMED", "UNJUST", "UNJUSTIFIED", "UNKIND", "UNLAWFUL",
			"UNLUCKY", "UNMOVED", "UNNATURAL", "UNNECESSARY", "UNNERVE", "UNNOTICED", "UNOBSERVED", "UNPLEASANT",
			"UNPOPULAR", "UNPROFITABLE", "UNQUALIFIED", "UNREASONABLE", "UNRELIABILITY", "UNRELIABLE", "UNREST",
			"UNRULY", "UNSAFE", "UNSATISFACTORY", "UNSCRUPULOUS", "UNSEEN", "UNSETTLING", "UNSOUND", "UNSPEAKABLE",
			"UNSTABLE", "UNSTEADINESS", "UNSTEADY", "UNSUCCESSFUL", "UNTIMELY", "UNTRAINED", "UNTRUE", "UNTRUSTWORTHY",
			"UNTRUTH", "UNWILLING", "UNWILLINGNESS", "UNWISE", "UNWORTHY", "UPHEAVAL", "UPRISING", "UPROAR", "UPROOT",
			"UPSET#1", "UPSET#2", "UPSET#3", "UPSET#4", "UPSET#5", "USELESS", "USURP", "UTTERANCE", "VAGABOND",
			"VAGRANT", "VAGUE", "VAGUENESS", "VAIN", "VANISH", "VANITY", "VEHEMENT", "VENGEANCE", "VENOM", "VENOMOUS",
			"VETO", "VEX", "VEXATION", "VEXING", "VICE#1", "VICIOUS", "VIE", "VILE", "VILLAIN", "VIOLATE", "VIOLATION",
			"VIOLENCE", "VIOLENT", "VIPER", "VOID", "VOLATILE", "VOLATILITY", "VOMIT", "VULGAR", "WAIL", "WAIT#4",
			"WALLOW", "WANE", "WANTON", "WAR", "WARFARE", "WARLIKE", "WARP", "WARY", "WASTE#1", "WASTE#2", "WASTEFUL",
			"WASTEFULNESS", "WAYWARD", "WEAKEN", "WEAR#2", "WEARINESS", "WEARISOME", "WEE", "WEED", "WEEP", "WEIRD",
			"WENCH", "WHACK", "WHIMPER", "WHINE", "WHIP#1", "WHIP#2", "WHIP#3", "WICKED", "WICKEDNESS", "WILD#1",
			"WILD#2", "WILD#3", "WILD#4", "WILT", "WILY", "WINCE", "WITCHCRAFT", "WITHHELD", "WITHHOLD", "WOE",
			"WOEFUL", "WORN#2", "WORN#3", "WORRIER", "WORRY#1", "WORRY#2", "WORRY#3", "WORRY#4", "WORRY#5", "WORSE",
			"WORSEN", "WORST", "WORTHLESS", "WOUND#1", "WOUND#3", "WOUND#5", "WRATH", "WRECK", "WRESTLE", "WRETCH",
			"WRETCHEDNESS", "WRINKLE", "WRITHE", "WRONG#2", "WRONG#3", "WRONGFUL", "WROUGHT", "YAWN", "YEARN", "YELP" };
}



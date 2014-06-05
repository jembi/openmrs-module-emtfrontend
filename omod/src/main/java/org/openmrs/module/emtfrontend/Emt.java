package org.openmrs.module.emtfrontend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class Emt {

	public static void main(String[] args) {
		try {
			Date startDate = Constants.shortDf.parse(args[0]);
			Date endDate = Constants.shortDf.parse(args[1]);

			loadConfig();
			// add one day minus 1 second to end date to easily include end date
			// in
			// calculations
			Calendar c = Calendar.getInstance();
			c.setTime(endDate);
			c.add(Calendar.HOUR, 24);
			c.add(Calendar.SECOND, -1);
			endDate = c.getTime();

			String emtLog = args[2];
			Emt emt = new Emt();
			emt.parseLog(startDate, endDate, emtLog);

			// todo set hours, minutes, seconds of start and end dates to outer
			// ranges of period
			Emt emtThisWeek = new Emt();
			c = Calendar.getInstance();
			c.setTime(new Date());
			int i = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
			c.add(Calendar.DATE, -i + 1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			Date start = c.getTime();
			c.add(Calendar.DATE, 6);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			Date end = c.getTime();
			emtThisWeek.parseLog(start, end, emtLog);
			String thisWeekUptime = emtThisWeek.systemUptime(start, end).print() + " ("
					+ Constants.df.format(start) + " - " + Constants.df.format(end) + ")";

			Emt emtPreviousWeek = new Emt();
			c = Calendar.getInstance();
			c.setTime(new Date());
			i = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
			c.add(Calendar.DATE, -i - 7 + 1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			start = c.getTime();
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			c.add(Calendar.DATE, 6);
			end = c.getTime();
			emtPreviousWeek.parseLog(start, end, emtLog);
			String previousWeekUptime = emtPreviousWeek
					.systemUptime(start, end).print()
					+ " ("
					+ Constants.df.format(start)
					+ " - "
					+ Constants.df.format(end) + ")";

			Emt emtPreviousMonth = new Emt();
			c = Calendar.getInstance();
			c.setTime(new Date());
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.add(Calendar.MONTH, -1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			start = c.getTime();
			c.add(Calendar.MONTH, 1);
			c.add(Calendar.DAY_OF_YEAR, -1);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			end = c.getTime();
			emtPreviousMonth.parseLog(start, end, emtLog);
			String previousMonthUptime = emtPreviousMonth.systemUptime(start,
					end).print()
					+ " ("
					+ Constants.df.format(start)
					+ " - "
					+ Constants.df.format(end)
					+ ")";

			List<String> s = emt.report(startDate, endDate, thisWeekUptime,
					previousWeekUptime, previousMonthUptime);
			System.out.println(s);
			String emtPdfOutput = args[3];
			emt.generatePdfReport(s, startDate, endDate, emtPdfOutput);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void loadConfig() {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(Constants.RUNTIME_DIR
					+ "/emt.properties");
			prop.load(input);
			clinicDays = prop.getProperty("clinicDays", "Mo,Tu,We,Th,Fr");
			clinicStart = Integer
					.parseInt(prop.getProperty("clinicStart", "8"));
			clinicStop = Integer.parseInt(prop.getProperty("clinicEnd", "17"));
		} catch (IOException ex) {
			System.out.println("Warning: " + Constants.RUNTIME_DIR
					+ "/emt.properties not found. Assuming defaults");
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	Date now = new Date();
	String systemId = "";
	public static String clinicDays = "Mo,Tu,We,Th,Fr";
	public static int clinicStart = 800;
	public static int clinicStop = 1700;

	int startupCount = 0;
	int shutdownCount = 0;
	int startupsWithoutShutdowns = 0;
	List<Startup> startups = new ArrayList<Startup>();
	List<Heartbeatable> heartbeats = new ArrayList<Heartbeatable>();
	List<Heartbeatable> openmrsHeartbeats = new ArrayList<Heartbeatable>();

	private void parseLog(Date startDate, Date endDate, String emtLog)
			throws FileNotFoundException {
		File emt = new File(emtLog);
		Scanner scanner = new Scanner(emt);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			try {
				StringTokenizer st = new StringTokenizer(line, ";", false);
				// timestamp
				String timestamp = "";
				if (st.hasMoreTokens()) {
					timestamp = st.nextToken();
					if (Helper.inPeriod(startDate, endDate, timestamp)) {
						// system id
						if (st.hasMoreTokens()) {
							systemId = st.nextToken();
						}
						// event type
						if (st.hasMoreTokens()) {
							String type = st.nextToken().trim();
							if ("STARTUP".equals(type)) {
								startupCount++;
								if (st.hasMoreTokens()) {
									String s = st.nextToken();
									if ("DIRTY".equals(s)) {
										startupsWithoutShutdowns++;
										startups.add(new Startup(timestamp,
												true));
									} else {
										startups.add(new Startup(timestamp,
												false));
									}
								} else {
									startups.add(new Startup(timestamp, false));
								}
							} else if ("SHUTDOWN".equals(type)) {
								shutdownCount++;
							} else if ("HEARTBEAT".equals(type)) {
								Heartbeat hb = new Heartbeat(timestamp, st);
								heartbeats.add(hb);
							} else if ("OPENMRS-HEARTBEAT".equals(type)) {
								OpenmrsHeartbeat hb = new OpenmrsHeartbeat(
										timestamp, st);
								openmrsHeartbeats.add(hb);
							} else if ("EMT-INSTALL".equals(type)
									|| "EMT-CONFIGURE".equals(type)) {

							} else {
								System.out.println("Unknown type '" + type
										+ "' found, ignoring");
							}
						}
					}
				}
			} catch (Exception e) {
				System.out.println("Error (" + e.getMessage() + ", "
						+ e.getCause() + ") parsing line: " + line);
				e.printStackTrace();
			}
		}
	}

	private List<String> report(Date startDate, Date endDate,
			String thisWeekUptime, String previousWeekUptime,
			String previousMonthUptime) {
		
		Uptime uptime = systemUptime(startDate, endDate);
		Uptime openmrsUptime = openmrsUptime(startDate, endDate);
		if (openmrsUptime.percentage > uptime.percentage) {
			openmrsUptime = uptime;
		}
		List<String> ss = new ArrayList<String>();
		ss.add("Current date and time: " + new Date());
		ss.add("");
		ss.add("\nSystem ID: " + systemId);
		ss.add("\nLast EMT installation date: <to be implemented>");
		ss.add("\nEMT version: " + emtVersion());
		ss.add("\nPrimary Clinic Days: " + clinicDays);
		ss.add("\nPrimary Clinic Hours: " + clinicStart + " - " + clinicStop);
		ss.add("");
		ss.add("\nStart date: " + Constants.df.format(startDate));
		ss.add("\nEnd date: " + Constants.df.format(endDate) + " (including)");
		ss.add("");
		ss.add("\nPercentage of system uptime (1): "
				+ uptime.print());
		ss.add("\n  This week: " + thisWeekUptime);
		ss.add("\n  Last week: " + previousWeekUptime);
		ss.add("\n  Last month: " + previousMonthUptime);
		ss.add("");
		ss.add("\nNumber of system starts (2): " + startupCount);
		ss.add("\nTimes of last system starts (2): " + lastSystemRestarts());
		ss.add("\nNumber of system starts without preceding shutdown (aka crashes) (2): "
				+ startupsWithoutShutdowns);
		ss.add("\nTimes of last system crashes (approximation) (2): <to be implemented>");
		ss.add("");
		ss.add("\nHighest average 5 minutes system loads (number of processors) (2): "
				+ highestAverage5minLoads());
		ss.add("\nLowest amounts of free memory in MB (2): "
				+ lowestFreeMemory());
		ss.add("");
		ss.add("\nPercentage of OpenMRS uptime (1): "
				+ openmrsUptime.print());
		ss.add("\nNumber of Encounters (3) - (4): "
				+ (totalEncounters(false) - totalEncounters(true)) + " - "
				+ totalEncounters(false));
		ss.add("\nNumber of Obs (3) - (4): "
				+ (totalObs(false) - totalObs(true)) + " - " + totalObs(false));
		ss.add("\nNumber of users (3) - (4): "
				+ (totalUsers(false) - totalUsers(true)) + " - "
				+ totalUsers(false));
		ss.add("\nNumber of active patients (3) - (4): "
				+ (totalActivePatients(false) - totalActivePatients(true)) + " - "
				+ totalActivePatients(false));
		ss.add("\nNumber of new patients (3) - (4): "
				+ (totalNewPatients(false) - totalNewPatients(true)) + " - "
				+ totalNewPatients(false));
		ss.add("\nNumber of visits (3) - (4): "
				+ (totalVisits(false) - totalVisits(true)) + " - "
				+ totalVisits(false));
		ss.add("\nLast local OpenMRS backup (5): " + lastOpenMRSBackup());
		ss.add("");
		ss.add("\n____");
		ss.add("");
		ss.add("\n(1) during clinic hours between start and end date");
		ss.add("\n(2) between start and end date (incl. outside of clinic hours)");
		ss.add("\n(3) new during period in OpenMRS database (not voided or retired)");
		ss.add("\n(4) total ever in OpenMRS database (not voided or retired)");
		ss.add("\n(5) in /var/backups/OpenMRS");

		return ss;
	}

	private String emtVersion() {
		return Constants.EMT_VERSION;
	}

	private int totalEncounters(boolean atStart) {
		if (atStart && openmrsHeartbeats.size() > 0) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(0)).totalEncounters;
		} else if (openmrsHeartbeats.size() > 1) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(openmrsHeartbeats.size() - 1)).totalEncounters;
		}
		return -1;
	}

	private int totalObs(boolean atStart) {
		if (atStart && openmrsHeartbeats.size() > 0) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(0)).totalObs;
		} else if (openmrsHeartbeats.size() > 1) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(openmrsHeartbeats.size() - 1)).totalObs;
		}
		return -1;
	}

	private int totalUsers(boolean atStart) {
		if (atStart && openmrsHeartbeats.size() > 0) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(0)).totalUsers;
		} else if (openmrsHeartbeats.size() > 1) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(openmrsHeartbeats.size() - 1)).totalUsers;
		}
		return -1;
	}

	private int totalActivePatients(boolean atStart) {
		if (atStart && openmrsHeartbeats.size() > 0) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(0)).activePatients;
		} else if (openmrsHeartbeats.size() > 1) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(openmrsHeartbeats.size() - 1)).activePatients;
		}
		return -1;
	}

	private int totalNewPatients(boolean atStart) {
		if (atStart && openmrsHeartbeats.size() > 0) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(0)).newPatients;
		} else if (openmrsHeartbeats.size() > 1) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(openmrsHeartbeats.size() - 1)).newPatients;
		}
		return -1;
	}

	private int totalVisits(boolean atStart) {
		if (atStart && openmrsHeartbeats.size() > 0) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(0)).visits;
		} else if (openmrsHeartbeats.size() > 1) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(openmrsHeartbeats.size() - 1)).visits;
		}
		return -1;
	}

	private void generatePdfReport(List<String> lines, Date startDate,
			Date endDate, String outputFilename) throws IOException,
			COSVisitorException, TransformerException {
		PDDocument document = new PDDocument();

		// metadata
		PDDocumentInformation info = document.getDocumentInformation();
		info.setAuthor("Christian Neumann");
		info.setTitle("EMT Report");
		// info.setSubject(subject);
		info.setProducer("EMR Monitoring Tool");
		info.setCreator(System.getProperty("user.name"));
		info.setCreationDate(Calendar.getInstance());
		document.setDocumentInformation(info);

		// content
		PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
		document.addPage(page);
		PDFont font = PDType1Font.HELVETICA;
		PDPageContentStream contentStream = new PDPageContentStream(document,
				page);

		if (lines.size() == 0) {
			contentStream.endText();
			contentStream.close();
			return;
		}
		final double fontHeight = font.getFontDescriptor().getFontBoundingBox()
				.getHeight() / 1000 * 12 * 0.865;

		contentStream.beginText();
		contentStream.setFont(font, 12);
		contentStream.appendRawCommands(fontHeight + " TL\n");
		contentStream.moveTextPositionByAmount(50, 750);
		for (int i = 0; i < lines.size(); i++) {
			contentStream.drawString(lines.get(i));
			if (i < lines.size() - 1) {
				contentStream.appendRawCommands("T*\n");
			}
		}
		contentStream.endText();
		contentStream.close();

		// Save the results and ensure that the document is properly closed:
		document.save(outputFilename);
		document.close();
	}

	private Uptime systemUptime(Date startDate, Date endDate) {
		Uptime uptime = new Uptime();
		uptime.calcHeartbearts(startDate, endDate, heartbeats, Constants.heartbeatCronjobIntervallInMinutes, Constants.firstHeartbeatCronjobStartsAtMinute);
		uptime.calcPercentage();
		return uptime;
	}

	private String lowestFreeMemory() {
		Collections.sort(heartbeats, new Comparator<Heartbeatable>() {
			public int compare(Heartbeatable hb1, Heartbeatable hb2) {
				return ((Integer) ((Heartbeat) hb1).freeMemory)
						.compareTo((Integer) ((Heartbeat) hb2).freeMemory);
			}
		});
		String s = "";
		s += heartbeats.size() > 0 ? ((Heartbeat) heartbeats.get(0)).freeMemory : "";
		s += " " + (heartbeats.size() > 1 ? ((Heartbeat) heartbeats.get(1)).freeMemory : "");
		s += " " + (heartbeats.size() > 2 ? ((Heartbeat) heartbeats.get(2)).freeMemory : "");
		return s;
	}

	private String lastSystemRestarts() {
		Collections.sort(startups, new Comparator<Startup>() {
			public int compare(Startup s1, Startup s2) {
				return ((Date) s1.date).compareTo((Date) s2.date);
			}
		});
		String s = "";
		s += startups.size() > 0 ? Constants.sdf.format(startups.get(0).date) : "";
		s += " "
				+ (startups.size() > 1 ? Constants.sdf.format(startups.get(1).date) : "");
		s += " "
				+ (startups.size() > 2 ? Constants.sdf.format(startups.get(2).date) : "");
		return s;
	}

	private String highestAverage5minLoads() {
		Collections.sort(heartbeats, new Comparator<Heartbeatable>() {
			public int compare(Heartbeatable hb1, Heartbeatable hb2) {
				return ((Double) ((Heartbeat) hb1).loadAverage5Min)
						.compareTo((Double) ((Heartbeat) hb2).loadAverage5Min);
			}
		});
		String s = "";
		s += heartbeats.size() > 0 ? ((Heartbeat) heartbeats.get(heartbeats.size() - 1)).loadAverage5Min
				: "";
		s += " "
				+ (heartbeats.size() > 1 ? ((Heartbeat) heartbeats
						.get(heartbeats.size() - 2)).loadAverage5Min : "");
		s += " "
				+ (heartbeats.size() > 2 ? ((Heartbeat) heartbeats
						.get(heartbeats.size() - 3)).loadAverage5Min : "");
		s += " ("
				+ (heartbeats.size() > 0 ? ((Heartbeat) heartbeats.get(0)).numberProcessors
						: "") + ")";
		return s;
	}

	private String lastOpenMRSBackup() {
		File fs = new File("/var/backups/OpenMRS");
		// File fs = new File("/tmp");
		File[] allFiles = fs.listFiles();
		if (allFiles != null && allFiles.length > 0) {
			Arrays.sort(allFiles, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return ((Long) f1.lastModified()).compareTo((Long) f2
							.lastModified());
				}
			});
			String s = allFiles[allFiles.length - 1].getName()
					+ " ("
					+ Constants.sdf.format(new Date(allFiles[allFiles.length - 1]
							.lastModified())) + ")";
			return s;
		} else {
			return "<not found>";
		}
	}

	private Uptime openmrsUptime(Date startDate, Date endDate) {
		Uptime uptime = new Uptime();
		uptime.calcHeartbearts(startDate, endDate, openmrsHeartbeats, Constants.openmrsHeartbeatCronjobIntervallInMinutes, Constants.firstOpenmrsHeartbeatCronjobStartsAtMinute);
		uptime.calcPercentage();
		return uptime;
	}
}

package mining.stackoverflow;

import java.lang.String;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * This class does some processing over some Stack Overflow questions and
 * answers, and finds: 1) the TOP_USERS users with the highest total score for
 * their answers written in the last PAST_MONTHS months; 2) the TOP_USERS users
 * with the highest total score for their questions written in the last
 * PAST_MONTHS months.
 *
 * These results are output in order of score.
 */
public class ServerFault {
	public static final int PAST_MONTHS = 6;
	public static final int TOP_USERS = 10;
	public static int ID_RANGE = 700000;
	public static int NUMBER_OF_FILES = 10;
	public static String QUESTION_FILE_PATH = ".\\UserData\\QuestionUserData";
	public static String ANSWER_FILE_PATH = ".\\UserData\\AnswerUserData";

	/**
	 * From XML input, get the value corresponding to the given key
	 */
	private static String parseFieldFromLine(String line, String key) {
		// We're looking for a thing that looks like:
		// [key]="[value]"
		// as part of a larger String.
		// We are given [key], and want to return [value].

		// Find the start of the pattern
		String keyPattern = key + "=\"";
		int idx = line.indexOf(keyPattern);

		// No match
		if (idx == -1) {
			return null;
		}

		// Find the closing quote at the end of the pattern
		int start = idx + keyPattern.length();

		int end = start;
		while (line.charAt(end) != '"') {
			end++;
		}

		// Extract [value] from the overall String and return it
		return line.substring(start, end);
	}

	/**
	 * Read all users from the input XML file and return them in a list
	 */
	public static List<User> readUsers(String filename)
			throws FileNotFoundException, IOException {
		final List<User> users = new ArrayList<User>();
		BufferedReader b = new BufferedReader(new InputStreamReader(
				new FileInputStream(filename), Charset.forName("UTF-8")));

		String line;
		while ((line = b.readLine()) != null) {
			String id = parseFieldFromLine(line, "Id");
			String displayName = parseFieldFromLine(line, "DisplayName");
			if (id != null || displayName != null) {
				users.add(new User(id, displayName));
			}
		}
		return users;
	}

	/**
	 * Read all posts from the input XML file and return them in a list
	 */
	public static List<Post> readPosts(String filename)
			throws FileNotFoundException, IOException, ParseException {
		final List<Post> posts = new ArrayList<Post>();
		BufferedReader b = new BufferedReader(new InputStreamReader(
				new FileInputStream(filename), Charset.forName("UTF-8")));

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS");

		String line;
		while ((line = b.readLine()) != null) {
			String id = parseFieldFromLine(line, "Id");
			String postType = parseFieldFromLine(line, "PostTypeId");
			String ownerId = parseFieldFromLine(line, "OwnerUserId");

			if (id == null || postType == null || ownerId == null) {
				continue;
			}

			Date creationDate = dateFormat.parse(parseFieldFromLine(line,
					"CreationDate"));
			int score = Integer.parseInt(parseFieldFromLine(line, "Score"));

			posts.add(new Post(id, Post.PostType.parse(postType), ownerId,
					creationDate, score));
		}
		b.close();
		return posts;
	}

	/**
	 * Look up a user by their ID
	 */
	public static User findUser(List<User> users, long id) {
		int low = 0;
		int high = users.size();
		while (low < high) {
			int mid = (low + high) / 2;
			long userId = Long.parseLong(users.get(mid).getId());
			if (userId < id) {
				low = mid;
			} else if (userId > id) {
				high = mid;
			} else {
				return users.get(mid);
			}
		}
		return null;
	}

	/**
	 * Collect the top TOP_USERS users by number of answers, and top TOP_USERS
	 * users by number of questions, (all on posts from the past PAST_MONTHS
	 * months) from the input user file and input posts file.
	 */

	public static TopUsers getTopUsers(String userFile, String postsFile)
			throws FileNotFoundException, IOException, ParseException {
		// Load data
		final List<User> users = readUsers(userFile);
		final List<Post> posts = readPosts(postsFile);

		// Find the date from M months ago
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -PAST_MONTHS);
		final Date nMonthsAgo = cal.getTime();

		// Calculate the total score of questions per user
		Map<String, UserData> questions = new HashMap<String, UserData>();

		for (Post p : posts) {
			User poster = findUser(users, Long.parseLong(p.getOwnerUserId()));
			if (poster == null) {
				continue;
			}

			if (questions.get(poster.getId()) == null) {
				questions.put(poster.getId(),
						new UserData(poster.getDisplayName()));
			}

			if (p.isQuestion() && p.getCreationDate().after(nMonthsAgo)) {
				questions.get(poster.getId()).addPostScore(p.getScore());
			}
		}

		// Collect the top TOP_USERS users by total score of their questions
		List<UserData> byTotalQuestionsScore = new ArrayList<UserData>();
		for (int i = 0; i < TOP_USERS; i++) {
			String key = null;
			UserData maxData = null;

			for (Map.Entry<String, UserData> it : questions.entrySet()) {
				if (key == null
						|| it.getValue().getScore() >= maxData.getScore()) {
					key = it.getKey();
					maxData = it.getValue();
				}
			}

			if (key != null) {
				questions.remove(key);
				byTotalQuestionsScore.add(maxData);
			}
		}

		// Calculate the total score of answers per user
		Map<String, UserData> answers = new HashMap<String, UserData>();
		;
		for (int i = 0; i < posts.size(); i++) {
			Post p = posts.get(i);
			User poster = findUser(users, Long.parseLong(p.getOwnerUserId()));
			if (poster == null) {
				continue;
			}

			if (answers.get(poster.getId()) == null) {
				answers.put(poster.getId(),
						new UserData(poster.getDisplayName()));
			}

			if (p.isAnswer() && p.getCreationDate().after(nMonthsAgo)) {
				answers.get(poster.getId()).addPostScore(p.getScore());
			}
		}

		// Collect the top TOP_USERS users by total score of their answers
		List<UserData> byTotalAnswersScore = new ArrayList<UserData>();
		for (int i = 0; i < TOP_USERS; i++) {
			String key = null;
			UserData maxData = null;

			for (Map.Entry<String, UserData> it : answers.entrySet()) {
				if (key == null
						|| it.getValue().getScore() >= maxData.getScore()) {
					key = it.getKey();
					maxData = it.getValue();
				}
			}

			if (key != null) {
				answers.remove(key);
				byTotalAnswersScore.add(maxData);
			}
		}
		return new TopUsers(byTotalQuestionsScore, byTotalAnswersScore);
	}

	/**
	 * Collect the top ALL users by number of answers, and top TOP_USERS users
	 * by number of questions, (all on posts from the past PAST_MONTHS months)
	 * from the input user file and input posts file.
	 * 
	 * @return
	 */
	public static TopUsers getAllUsersDataOfEachFile(List<User> users,
			String postsFile) throws FileNotFoundException, IOException,
			ParseException {
		// Load data
		final List<Post> posts = readPosts(postsFile);

		// Find the date from M months ago
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -PAST_MONTHS);
		final Date nMonthsAgo = cal.getTime();

		// Calculate the total score of questions per user
		Map<String, UserData> questions = new HashMap<String, UserData>();

		for (Post p : posts) {
			User poster = findUser(users, Long.parseLong(p.getOwnerUserId()));
			if (poster == null) {
				continue;
			}
			if (questions.get(poster.getId()) == null) {
				questions.put(poster.getId(), new UserData(poster.getId(),
						poster.getDisplayName()));
			}
			if (p.isQuestion() && p.getCreationDate().after(nMonthsAgo)) {
				questions.get(poster.getId()).addPostScore(p.getScore());
			}
		}

		// Collect the top TOP_USERS users by total score of their questions
		List<UserData> byTotalQuestionsScore = new ArrayList<UserData>();
		byTotalQuestionsScore.addAll(questions.values());
		Collections.sort(byTotalQuestionsScore);

		// Calculate the total score of answers per user
		Map<String, UserData> answers = new HashMap<String, UserData>();

		for (int i = 0; i < posts.size(); i++) {
			Post p = posts.get(i);
			User poster = findUser(users, Long.parseLong(p.getOwnerUserId()));
			if (poster == null) {
				continue;
			}
			if (answers.get(poster.getId()) == null) {
				answers.put(poster.getId(),
						new UserData(poster.getId(), poster.getDisplayName()));
			}
			if (p.isAnswer() && p.getCreationDate().after(nMonthsAgo)) {
				answers.get(poster.getId()).addPostScore(p.getScore());
			}
		}
		// Collect the top TOP_USERS users by total score of their answers
		List<UserData> byTotalAnswersScore = new ArrayList<UserData>();
		byTotalAnswersScore.addAll(answers.values());
		Collections.sort(byTotalAnswersScore);

		return new TopUsers(byTotalQuestionsScore, byTotalAnswersScore);
	}

	private static void WriteUserDataToFile(
			List<UserData> byTotalQuestionsScore,
			List<UserData> byTotalAnswersScore) {

		try {
			List<String> listOfOutputFiles = new ArrayList<String>();
			for (int i = 0; i < NUMBER_OF_FILES; i++) {
				listOfOutputFiles.add(QUESTION_FILE_PATH + i + ".txt");
			}
			Map<Integer, List<UserData>> idMap = seperatedById(byTotalQuestionsScore);

			for (Integer id : idMap.keySet()) {
				writeToFile(listOfOutputFiles.get(id), idMap.get(id));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			List<String> listOfOutputFiles = new ArrayList<String>();
			for (int i = 0; i < NUMBER_OF_FILES; i++) {
				listOfOutputFiles.add(ANSWER_FILE_PATH + i + ".txt");
			}
			Map<Integer, List<UserData>> idMap = seperatedById(byTotalAnswersScore);

			for (Integer id : idMap.keySet()) {
				writeToFile(listOfOutputFiles.get(id), idMap.get(id));
			}
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	private static Map<Integer, List<UserData>> seperatedById(
			List<UserData> byTotalQuestionsScore) {
		Map<Integer, List<UserData>> idMap = new HashMap<Integer, List<UserData>>();
		for (UserData userdata : byTotalQuestionsScore) {
			long id = Long.parseLong(userdata.getId());
			for (int i = 0; i < NUMBER_OF_FILES; i++) {
				if (id >= ID_RANGE * i && id < ID_RANGE * (i + 1)) {
					if (idMap.get(i) == null) {
						List<UserData> ls = new ArrayList<UserData>();
						ls.add(userdata);
						idMap.put(new Integer(i), ls);
					} else {
						idMap.get(i).add(userdata);
					}
					break;
				}
			}
		}
		return idMap;
	}

	private static void writeToFile(String filePath, List<UserData> groupedData)
			throws IOException {

		List<UserData> dataFromFile = new ArrayList<UserData>();
		Path path = Paths.get(filePath);
		if (Files.exists(path)) {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] contents = line.split(",");
				UserData data = new UserData(contents[0], contents[1],
						Long.parseLong(contents[2]));
				dataFromFile.add(data);

			}
			br.close();
		}
		for (UserData ud : groupedData) {
			int index = dataFromFile.indexOf(ud);
			if (index != -1) {
				UserData usr = dataFromFile.get(index);
				usr.addPostScore((int) ud.getScore());
			} else {
				dataFromFile.add(ud);
			}
		}
		Collections.sort(dataFromFile);
		BufferedWriter wr = new BufferedWriter(new FileWriter(filePath));
		for (int i = 0; i < dataFromFile.size() - 1; i++) {
			UserData data = dataFromFile.get(i);
			wr.write(data.getId() + "," + data.getDisplayName() + ","
					+ data.getScore() + "\n");
		}
		UserData data = dataFromFile.get(dataFromFile.size() - 1);
		wr.write(data.getId() + "," + data.getDisplayName() + ","
				+ data.getScore());
		wr.close();

	}

	public static void main(String[] args) throws FileNotFoundException,

	IOException, ParseException {
		if (args.length < 2) {
			System.err
					.println("Error: usage: <users file> <posts file1> <posts file2> ... <posts file n>");
			System.exit(1);
		}
		final List<User> users = readUsers(args[0]);
		Collections.sort(users);
		// System.out.println(users.get(0));
		for (int i = 1; i < args.length; i += 1) {
			TopUsers results = ServerFault.getAllUsersDataOfEachFile(users,
					args[i]);
			ServerFault.WriteUserDataToFile(results.getUsersByQuestions(),
					results.getUsersByAnswers());
		}
		List<List<UserData>> listOfTopUsersByQuestion = new ArrayList<List<UserData>>();
		List<List<UserData>> listOfTopUsersByAnswer = new ArrayList<List<UserData>>();
		for (int i = 0; i < NUMBER_OF_FILES; i++) {
			listOfTopUsersByQuestion.add(ServerFault
					.getTopUsers(QUESTION_FILE_PATH + i + ".txt"));
			listOfTopUsersByAnswer.add(ServerFault.getTopUsers(ANSWER_FILE_PATH
					+ i + ".txt"));
		}
		List<UserData> mergedTopUsersByQuestion = ServerFault
				.MergeSortedLists(listOfTopUsersByQuestion);
		System.out.println("Top " + TOP_USERS
				+ " users with the highest total score among questions:");
		if (mergedTopUsersByQuestion != null) {
			for (int i = 0; i < Math.min(TOP_USERS,
					mergedTopUsersByQuestion.size()); i++) {
				UserData dt = mergedTopUsersByQuestion.get(i);
				System.out.println("Id:" + dt.getId() + "\tScore: "
						+ dt.getScore() + "\tName: " + dt.getDisplayName());
			}
		}
		System.out.println("\n");
		List<UserData> mergedTopUsersByAnswer = ServerFault
				.MergeSortedLists(listOfTopUsersByAnswer);
		System.out.println("Top " + TOP_USERS
				+ " users with the highest total score among answers:");
		if (mergedTopUsersByAnswer != null) {
			for (int i = 0; i < Math.min(TOP_USERS,
					mergedTopUsersByAnswer.size()); i++) {
				UserData dt = mergedTopUsersByAnswer.get(i);
				System.out
						.println("Id:" + dt.getId() + "\tScore: "
								+ dt.getScore() + "\t" + "Name: "
								+ dt.getDisplayName());
			}
		}
		removeIntermidiateFiles();
	}

	private static void removeIntermidiateFiles() {
		try {
			for (int i = 0; i < NUMBER_OF_FILES; i++) {
				Path QuestionFileName = Paths.get(QUESTION_FILE_PATH + i
						+ ".txt");
				Path AnswerFileName = Paths.get(ANSWER_FILE_PATH + i + ".txt");
				if (Files.exists(QuestionFileName)) {
					Files.delete(QuestionFileName);
				}
				if (Files.exists(AnswerFileName)) {
					Files.delete(AnswerFileName);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<UserData> getTopUsers(String filePath)
			throws NumberFormatException, IOException {
		List<UserData> dataFromFile = new ArrayList<UserData>();
		Path path = Paths.get(filePath);
		if (Files.exists(path)) {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			int count = 0;
			String line;
			while ((line = br.readLine()) != null && count < TOP_USERS) {
				if (line != null) {
					String[] contents = line.split(",");
					UserData data = new UserData(contents[0], contents[1],
							Long.parseLong(contents[2]));
					dataFromFile.add(data);
					count++;
				}
			}
			br.close();
		}
		return dataFromFile;
	}

	private static List<UserData> MergeSortedLists(
			List<List<UserData>> listOfTops) {
		List<UserData> mergedResult = new ArrayList<UserData>();
		PriorityQueue<UserData> priQue = new PriorityQueue<UserData>(
				listOfTops.size());
		for (List<UserData> ls : listOfTops) {
			if (ls.size() > 0)
				priQue.add(ls.get(0));
		}
		while (!priQue.isEmpty()) {
			UserData poppedItem = priQue.poll();
			mergedResult.add(poppedItem);
			for (List<UserData> ls : listOfTops) {
				if (ls.contains(poppedItem)) {
					ls.remove(poppedItem);
					if (!ls.isEmpty())
						priQue.add(ls.get(0));
					break;
				}
			}
		}
		return mergedResult;
	}
}

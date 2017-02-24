package mining.stackoverflow;

/**
 * Link a user name with a number of answers / questions
 */
public class UserData implements Comparable<UserData> {
	private String id;
	private final String displayName;
	private long score;

	public UserData(String id, String displayName) {
		this.id = id;
		this.displayName = displayName;
		this.score = 0;
	}

	public UserData(String id, String displayName, long score) {
		this.id = id;
		this.displayName = displayName;
		this.score = score;
	}

	public UserData(String displayName) {
		this.displayName = displayName;
		this.score = 0;
	}

	public void addPostScore(int postScore) {
		score += postScore;
	}

	public String getDisplayName() {
		return displayName;
	}

	public long getScore() {
		return score;
	}

	@Override
	public int compareTo(UserData o) {
		// if (Long.parseLong(o.id) > Long.parseLong(id)) {
		// return 1;
		// } else if (Long.parseLong(o.id) < Long.parseLong(id)) {
		// return -1;
		// }
		if (o.getScore() > score) {
			return 1;
		} else if (o.getScore() < score) {
			return -1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof UserData) {
			if (((UserData) o).id.equals(id))
				return true;
		}
		return false;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return id + "," + displayName + "," + score;
	}
}

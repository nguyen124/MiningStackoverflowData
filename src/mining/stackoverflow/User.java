package mining.stackoverflow;

/**
 * Represents a user of Stack Overflow.
 *
 * There is one instance of this per entry in users.xml. Some fields omitted due
 * to laziness.
 */
public class User implements Comparable<User> {
	private final String id;
	private final String displayName;

	public User(String id, String displayName) {
		this.id = id;
		this.displayName = displayName;
	}

	public String getId() {
		return id.trim();
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return id + "," + displayName;
	}

	@Override
	public int compareTo(User o) {
		if (Long.parseLong(o.id) < Long.parseLong(id)) {
			return 1;
		} else if (Long.parseLong(o.id) > Long.parseLong(id)) {
			return -1;
		}
		return 0;
	}
};

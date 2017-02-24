package mining.stackoverflow;

import java.util.Date;

/**
 * Represents a post on Stack Overflow.
 *
 * There is one of these for each entry in posts.xml. Some fields omitted due to
 * laziness.
 */
public class Post {
	public enum PostType {
		QUESTION, ANSWER, OTHER;

		public static PostType parse(String from)
				throws IllegalArgumentException {
			switch (from) {
			case "1":
				return QUESTION;
			case "2":
				return ANSWER;
			default:
				return OTHER;
			}
		}
	}

	private final String id;
	private final PostType type;
	private final String ownerUserId;
	private final Date creationDate;
	private final int score;

	public Post(String id, PostType type, String ownerUserId,
			Date creationDate, int score) {
		this.id = id;
		this.type = type;
		this.ownerUserId = ownerUserId;
		this.creationDate = creationDate;
		this.score = score;
	}

	public String getId() {
		return id;
	}

	public PostType getType() {
		return type;
	}

	public String getOwnerUserId() {
		return ownerUserId;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public int getScore() {
		return score;
	}

	public boolean isQuestion() {
		return type.equals(PostType.QUESTION);
	}

	public boolean isAnswer() {
		return type.equals(PostType.ANSWER);
	}

	@Override
	public String toString() {
		return ownerUserId + "," + type + "," + score;
	}
}

package org.hive2hive.core.exceptions;

public enum AbortModificationCode implements ErrorCode {

	UNSPECIFIED(200),
	SAME_CONTENT(201),
	NO_WRITE_PERM(202),
	FILE_INDEX_NOT_FOUND(203),
	NON_EMPTY_DIR(204),
	FOLDER_UPDATE(205),
	ROOT_DELETE_ATTEMPT(206);

	private final int number;

	private AbortModificationCode(int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}
}

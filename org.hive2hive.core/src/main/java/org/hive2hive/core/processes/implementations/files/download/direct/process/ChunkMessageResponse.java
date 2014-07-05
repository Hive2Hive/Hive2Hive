package org.hive2hive.core.processes.implementations.files.download.direct.process;

import java.io.Serializable;

import org.hive2hive.core.model.Chunk;

public class ChunkMessageResponse implements Serializable {

	enum AnswerType {
		OK,
		DECLINED,
		ASK_LATER
	}

	private final AnswerType answerType;
	private final Chunk chunk;

	public ChunkMessageResponse(Chunk chunk) {
		this.chunk = chunk;
		this.answerType = AnswerType.OK;
	}

	public ChunkMessageResponse(AnswerType answerType) {
		this.chunk = null;
		this.answerType = answerType;
	}

	public AnswerType getAnswerType() {
		return answerType;
	}

	public Chunk getChunk() {
		return chunk;
	}
}

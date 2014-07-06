package org.hive2hive.core.processes.files.download.direct.process;

import java.io.Serializable;

import org.hive2hive.core.model.Chunk;

/**
 * The response from a client that was asked to deliver a certain chunk.
 * 
 * @author Nico
 * 
 */
public class ChunkMessageResponse implements Serializable {

	private static final long serialVersionUID = -7732972005871878370L;

	enum AnswerType {
		OK,
		DECLINED,
		ASK_LATER
	}

	private final AnswerType answerType;
	private final Chunk chunk;

	/**
	 * Everything is ok and the chunk can be returned
	 * 
	 * @param chunk the chunk that was requested
	 */
	public ChunkMessageResponse(Chunk chunk) {
		this.chunk = chunk;
		this.answerType = AnswerType.OK;
	}

	/**
	 * The chunk cannot be returned for a certain reason.
	 * 
	 * @param answerType the type of error
	 */
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

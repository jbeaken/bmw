package org.bookmarks.website.search;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;

public class DefaultSimilarity extends TFIDFSimilarity {

	@Override
	public float queryNorm(float sumOfSquaredWeights) {
		return 0;
	}

	@Override
	public float sloppyFreq(int distance) {
		return 0;
	}

	@Override
	public float tf(float freq) {
		return 1.0f;
	}

	@Override
	public float coord(int overlap, int maxOverlap) {
		return 0;
	}

	@Override
	public float idf(long docFreq, long numDocs) {
		return 0;
	}

	@Override
	public float lengthNorm(FieldInvertState state) {
		return 0;
	}

	@Override
	public float decodeNormValue(long norm) {
		return 0;
	}

	@Override
	public long encodeNormValue(float f) {
		return 0;
	}

	@Override
	public float scorePayload(int doc, int start, int end, BytesRef payload) {
		return 0;
	}

}

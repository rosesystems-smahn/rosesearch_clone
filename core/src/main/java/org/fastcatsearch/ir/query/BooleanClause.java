package org.fastcatsearch.ir.query;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.StopwordAttribute;
import org.apache.lucene.analysis.tokenattributes.SynonymAttribute;
import org.apache.lucene.util.CharsRef;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.CharVectorTokenizer;
import org.fastcatsearch.ir.query.Term.Option;
import org.fastcatsearch.ir.search.PostingDocs;
import org.fastcatsearch.ir.search.SearchIndexReader;
import org.fastcatsearch.ir.settings.IndexSetting;
import org.fastcatsearch.ir.settings.RefSetting;

public class BooleanClause implements OperatedClause {

	
	private OperatedClause operatedClause;
	
	public BooleanClause(SearchIndexReader searchIndexReader, Term term, HighlightInfo highlightInfo) {
		String indexId = searchIndexReader.indexId();
		String termString = term.termString();
		int weight = term.weight();
		Option option = term.option();
		
		CharVector fullTerm = new CharVector(termString);
		Analyzer analyzer = searchIndexReader.getQueryAnalyzerFromPool();
		
		IndexSetting indexSetting = searchIndexReader.indexSetting();
		if (highlightInfo != null) {
			String queryAnalyzerName = indexSetting.getQueryAnalyzer();
			for (RefSetting refSetting : indexSetting.getFieldList()) {
				highlightInfo.add(refSetting.getRef(), queryAnalyzerName, term.termString());
			}
		}
		try {
			CharVectorTokenizer charVectorTokenizer = new CharVectorTokenizer(fullTerm);
			CharTermAttribute termAttribute = null;
			CharsRefTermAttribute refTermAttribute = null;
			PositionIncrementAttribute positionAttribute = null;
			SynonymAttribute synonymAttribute = null;
			StopwordAttribute stopwordAttribute = null;
			int positionOffset = 0;
			
			
			//어절로 분리.
			while (charVectorTokenizer.hasNext()) {
				CharVector eojeol = charVectorTokenizer.next();

				TokenStream tokenStream = analyzer.tokenStream(indexId, eojeol.getReader());
				tokenStream.reset();

				if (tokenStream.hasAttribute(CharsRefTermAttribute.class)) {
					refTermAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
				}
				if (tokenStream.hasAttribute(CharTermAttribute.class)) {
					termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
				}
				if (tokenStream.hasAttribute(PositionIncrementAttribute.class)) {
					positionAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
				}
				CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);

				if (tokenStream.hasAttribute(SynonymAttribute.class)) {
					synonymAttribute = tokenStream.getAttribute(SynonymAttribute.class);
				}
				if (tokenStream.hasAttribute(StopwordAttribute.class)) {
					stopwordAttribute = tokenStream.getAttribute(StopwordAttribute.class);
				}

				// PosTagAttribute tagAttribute = tokenStream.getAttribute(PosTagAttribute.class);
				
				CharVector token = null;
				while (tokenStream.incrementToken()) {

					if (refTermAttribute != null) {
						CharsRef charRef = refTermAttribute.charsRef();
						
						if(charRef!=null) {
							char[] buffer = new char[charRef.length()];
							System.arraycopy(charRef.chars, charRef.offset, buffer, 0, charRef.length);
							token = new CharVector(buffer, 0, buffer.length);
						} else if(termAttribute!=null && termAttribute.buffer()!=null) {
							token = new CharVector(termAttribute.buffer());
						}
					} else {
						token = new CharVector(charTermAttribute.buffer(), 0, charTermAttribute.length());
					}

					logger.debug("token = {}", token);
					// token.toUpperCase();
					//
					// stopword
					//
					if (option.useStopword() && stopwordAttribute != null && stopwordAttribute.isStopword()) {
						logger.debug("stopword : {}", token);
						continue;
					}

					int queryPosition = 0;
					if (positionAttribute != null) {
						int position = positionAttribute.getPositionIncrement();
						queryPosition = positionOffset + position; //
						positionOffset = position + 2; // 다음 position은 +2 부터 할당한다. 공백도 1만큼 차지.
					}
					
					PostingDocs postingDocs = searchIndexReader.getPosting(token);
					OperatedClause clause = new TermOperatedClause(postingDocs, weight);
					
					if(operatedClause == null){
						operatedClause = clause;
					}else{
						operatedClause = new AndOperatedClause(operatedClause, clause);
					}
				}
				
			}
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			searchIndexReader.releaseAnalyzerToPool(analyzer);
		}
	}

	@Override
	public boolean next(RankInfo docInfo) {
		if(operatedClause == null){
			return false;
		}
		return operatedClause.next(docInfo);
	}

}
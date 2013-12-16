package org.fastcatsearch.ir.dictionary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class SpaceDictionary extends MapDictionary {

	private Set<CharVector> wordSet;

	public SpaceDictionary() {
		wordSet = new HashSet<CharVector>();
	}

	public SpaceDictionary(File file) {
		super(file);
	}

	public SpaceDictionary(InputStream is) {
		super(is);
	}

	public Set<CharVector> getWordSet() {
		return wordSet;
	}
	
	public Set<CharVector> getUnmodifiableWordSet() {
		return Collections.unmodifiableSet(wordSet);
	}

	@Override
	public void addEntry(String keyword, Object[] ignoreValue, boolean ignoreCase, boolean[] valuesIgnoreCase) {
		CharVector[] value = makeValue(keyword, ignoreCase);
		CharVector key = makeKey(value);
		map.put(key, value);
	}
	
	private CharVector[] makeValue(String word, boolean ignoreCase) {
		String[] list = word.split(",");
		CharVector[] value = new CharVector[list.length];
		for(int i=0;i < list.length;i++){
			value[i] = new CharVector(list[i].trim());
			if(ignoreCase){
				value[i].toUpperCase();
			}
		}
		return value;
	}
	
	private CharVector makeKey(CharVector[] value) {
		String key = "";
		for(CharVector cv : value){
			key += cv.toString();
		}
		return new CharVector(key);
	}
	

	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		DataOutput output = new OutputStreamDataOutput(out);
		// write size of synonyms
		output.writeVInt(wordSet.size());

		// write synonyms
		Iterator<CharVector> synonymIter = wordSet.iterator();
		for (; synonymIter.hasNext();) {
			CharVector value = synonymIter.next();
			output.writeUString(value.array, value.start, value.length);
		}
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		DataInput input = new InputStreamDataInput(in);
		wordSet = new HashSet<CharVector>();
		int size = input.readVInt();
		for (int entryInx = 0; entryInx < size; entryInx++) {
			wordSet.add(new CharVector(input.readUString()));
		}
	}
}
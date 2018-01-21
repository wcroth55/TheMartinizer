package com.caucus.martinizer;

import static com.caucus.martinizer.ChangedCodeScanner.DiffState.*;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;


public class ChangedCodeScanner
{
	protected enum DiffState {  // States of finite-state machine, below.
		Unknown,  // Before the machine starts, or if we've determined we're in the midst of junk that can be ignored.
		Diff,     // Found a git 'diff' line, plucked out the info we needed, then ignoring the rest until we get to actual code.
		Code      // In the midst of actual code.
	}
	
	private String sourceJavaDir;
	
	public ChangedCodeScanner (String sourceJavaDir) {
		this.sourceJavaDir = sourceJavaDir;
	}
	
	public List<String> processGitDiffLines (BufferedReader reader, List<String> onlyTheseProjects) {
		List<String> results = new ArrayList<String>();
		String line;
		DiffState state = Unknown;
		
		ClassChangeRecord changeRecord = new ClassChangeRecord("", sourceJavaDir, onlyTheseProjects);
		int lineNumber = 0;
		boolean onlyMinus = true;      // was this line simply deleted?
		boolean lineChanged = false;   // was this line changed?
		
		// This loop is a simple finite-state machine that scans the git diff output one line at a time,
		// and modifies its state accordingly.  For each class, it creates a new ClassChangeRecord, and
		// each changed line number is added to it.  Once we finish scanning a class, that ClassChangeRecord
		// is added to the List of results, and a new, empty, ClassChangeRecord is constructed. 
		//
		// Since we're only interested in changed or added lines, we use 'onlyMinus' to keep track of cases
		// where lines were (only!) deleted... so we can throw that case out at the end.
		while ((line = readline(reader)) != null) {
			
			// Handle before the first 'diff', plus any non-java files.
			if (state == Unknown) {
				if (line.startsWith("diff")) {
					changeRecord = new ClassChangeRecord(line, sourceJavaDir, onlyTheseProjects);
					state = changeRecord.isValid() ? Diff : Unknown;
				}
			}
			
			// Handle anything after 'diff' until '@@', then switch to Code.
			else if (state == Diff) {
				if (line.startsWith("@@")) {
					lineNumber = extractLineNumber (line);
					state = Code;
					lineChanged = false;
				}
			}
			
			else if (state == Code) {
				if (line.startsWith("+")) {
					lineChanged = true;
					onlyMinus = false;
				}
				
				else if (line.startsWith("-")) {
					lineChanged = true;
				}
				
				else if (line.startsWith(" ") || line.startsWith("\t") || line.length() == 0) {
					onlyMinus = false;
				}
				
				else if (line.equals("~")) {
					if (!onlyMinus) {
						if (lineChanged)
							changeRecord.add(lineNumber);
						++lineNumber;
					}
					
					// Prepare initial state for next line.
					lineChanged = false;
					onlyMinus = true;
				}
				
				else if (line.startsWith("@@")) {  // will have seen ~ first.
					lineNumber = extractLineNumber (line);
					lineChanged = false;
				}
				
				else if (line.startsWith("diff")) {
					if (changeRecord.isValid()  &&  changeRecord.hasLineNumbers())
						results.add(changeRecord.toString());
					
					changeRecord = new ClassChangeRecord(line, sourceJavaDir, onlyTheseProjects);
					state = changeRecord.isValid() ? Diff : Unknown;
				}
			}
		}
		
		if (changeRecord.isValid()  &&  changeRecord.hasLineNumbers())
			results.add(changeRecord.toString());
		
		return results;
	}
	
	private String readline(BufferedReader reader) {
		String result = null;
		try {
			result = reader.readLine();
		} catch (Exception e) {}
		return result;
	}
	
	protected int extractLineNumber (String line) {
		String [] temp = StringUtils.split(line.replace(",", " "), " ");
		String num = temp[3];
		return NumberUtils.toInt(num, -1);
	}

}

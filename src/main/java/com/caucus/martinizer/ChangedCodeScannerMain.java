package com.caucus.martinizer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 * TheMartinizer:  find lines that changed in important Java classes between two commits to a project. 
 * 
 * Usage:
 *    Simple example:
 *       git diff --word-diff=porcelain old-commit-hash new-commit-hash | java -jar ChangedCodeScanner.jar
 *       
 *    produces output like:
 *       src com.caucus.javautils.io PqHttpClient 167 176
 *       
 *       (Ignore the 'src' part.  Otherwise the line says that, in the package com.caucus.javautils.io,
 *        the class PqHttpClient had changes or additions on lines 167 and 176.)
 *    
 *    A more complicated example allows for sub-projects underneath a single git repo.  E.g. I have a repo
 *    with multiple sub-projects: but I'm only interested in the CoreLibrary and WebApp sub-projects:
 * 
 *       git diff --word-diff=porcelain old-commit-hash new-commit-hash | \
 *          java -jar ChangedCodeScanner.jar CoreLibrary WebApp
 *       
 *    produces output like:
 *       CoreLibrary com.caucus.apps.corelib.search.impl SearchEngineImpl 45 46 47 83 205
 *       WebApp com.caucus.apps.onesearch.services.media RestrictionServiceImpl 24 25
 *    
 *    which means, 
 *       In the CoreLibrary sub-project SearchEngineImpl  class, the new-commit-hash has changed lines 45-47, and 205.
 *       In the WebApp sub-project RestrictionServiceImpl class, the new-commit-hash has changed lines 24 and 25.
 *       
 *    More precisely, note that "changed" means lines in which text was changed, or new lines that were added.
 *    It does *not* include lines that were deleted (going from old-commit-hash to new-commit-hash).
 *       
 * Notes:
 *    1. Only classes in the sub-projects listed on the command line are included (no matter what git reported).
 *    2. Test classes are ignored.  See ClassChangeRecord for the definition of test classes.
 *    3. Non-java files are ignored.
 *    
 * Purpose:
 *    The real purpose of ChangedCodeScanner is to act as a front-end to 'The Martinizer',
 *    so that we can match the ChangedCodeScanner output with the Jacoco coverage HTML output, and tell developers
 *    the Holy Grail of code coverage, to wit:
 *    
 *       "Which lines (that you changed) are NOT covered by unit-tests?"
 *       
 *    For more information and discussion, see http://lookfar.caucus.com/index.php/2018/01/16/unit-testing-the-martinizer/
 *    
 * License:
 *    The entire Martinizer project is Copyright (C) 2018 by Charles Roth (Ann Arbor MI), but is released as "Open Source", 
 *    under the "Artistic License 2.0" as described in detail at https://opensource.org/licenses/Artistic-2.0.  
 *    Briefly, you may copy, modify, or distribute the source code for this project for any purpose, 
 *    but you must maintain the attributions and license, and you must document and attribute any modifications.
 *    
 *    Nothing in this project implies any approval by, connection with, or ownership of, the term "Martinizer"
 *    as used in the dry-cleaning industry, nor with any person or persons named "Martin".  It's just a joke,
 *    guys!
 * 
 * @author Charles Roth.  First public version 2018-01-18
 */

public class ChangedCodeScannerMain
{

	public static void main(String[] args) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		ChangedCodeScanner codeScanner = new ChangedCodeScanner();
		
		List<String> results = codeScanner.processGitDiffLines(reader, args);
		for (String result: results) {
			System.out.println(result);
		}
	}
}

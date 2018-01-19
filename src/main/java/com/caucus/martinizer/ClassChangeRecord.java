package com.caucus.martinizer;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * A record of a set of changes (line numbers where the lines were changed) in a class.
 * 
 * The result (produced by toString()) looks like:
 *    subProjectName fullPackageName className line1 line2 ...
 *    
 * An instance of ClassChangeRecord may be 'valid' (useful), or 'invalid'
 * (e.g. a test class that we're going to throw away, or a class that is not in
 * the desired list of sub-projects).
 * 
 * @author Charles Roth
 */

public class ClassChangeRecord
{
	private static final String [] TEST_CLASS_DIRECTORIES 
			= {"/src/test/java", "/src/integration/java", "/src/slowtest/java", "/src/regression/java"};
	
	private String projectName;
	private String packageName;
	private String className;
	private StringBuffer lineNumbers;
	private boolean isValid;
	
	/**
	 * Constructor.
	 * @param line The entire git line starting with "diff..."
	 * @param keepOnlyTheseProjects varargs array of subprojects we're interested in.
	 *        If empty, there's only one project, no sub-projects.
	 */
	public ClassChangeRecord (String line, String ... keepOnlyTheseProjects) {
		lineNumbers = new StringBuffer();
		projectName = "";
		String [] temp = StringUtils.split(line, " ");
		if (temp == null  ||  temp.length < 4  ||  isTestDirectory(temp[3]))  isValid = false;
		else {
			projectName = (ArrayUtils.isEmpty(keepOnlyTheseProjects) ? "." : StringUtils.substringBetween(temp[3], "b/", "/"));
			packageName = StringUtils.substringAfter (temp[3], "/java/");
			packageName = StringUtils.substringBeforeLast(packageName, "/");
			packageName = packageName.replace("/", ".");
			className = StringUtils.substringAfterLast(temp[3], "/");
			
			isValid = className.endsWith(".java");
			className = StringUtils.substringBefore(className, ".java");
		}
		
		if (! projectIsIn (keepOnlyTheseProjects))
			isValid = false;
		
	}
	
	private boolean isTestDirectory (String path) {
		for (String testDir: TEST_CLASS_DIRECTORIES)  if (path.contains(testDir))  return true;
		return false;
	}
	
	private boolean projectIsIn (String ... keepers) {
		if (ArrayUtils.isEmpty(keepers))    return true;
		for (String keeper: keepers) {
			if (projectName.equals(keeper)) return true;
		}
		return false;
	}
	
	public boolean hasLineNumbers() {
		return lineNumbers.length() > 0;
	}
	
	public void add(int lineNumber) {
		lineNumbers.append(lineNumber + " ");
	}
	
	public boolean isValid() {
		return isValid;
	}
	
	public String toString() {
		return projectName + " " + packageName + " " + className + " " + lineNumbers.toString();
	}
	
}

package com.caucus.martinizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.caucus.martinizer.ClassChangeRecord;


public class ClassChangeRecordTest
{
	private static final String SRC_JAVA_DIR = "src/main/java";
	
	private List<String> subProjects;
	
	@Before
	public void setUp() {
		subProjects = new ArrayList<String>();
		subProjects.add("CoreLibrary");
	}
	
	@Test
	public void shouldBreakApartDiffLine_intoProjectPackageAndClass() {
		String diffLine = "diff --git a/CoreLibrary/src/main/java/com/caucus/apps/corelib/AppCoreLibModule.java b/CoreLibrary/src/main/java/com/caucus/apps/corelib/AppCoreLibModule.java";
		ClassChangeRecord changes = new ClassChangeRecord(diffLine, SRC_JAVA_DIR, subProjects);
		assertTrue   (changes.isValid());
		assertEquals ("CoreLibrary com.caucus.apps.corelib AppCoreLibModule ", changes.toString());
		assertFalse  (changes.hasLineNumbers());
		
		changes.add(17);
		assertTrue (changes.hasLineNumbers());
		assertEquals ("CoreLibrary com.caucus.apps.corelib AppCoreLibModule 17 ", changes.toString());
	}
	
	@Test
	public void shouldNotBeValid_ifNotJava() {
		String diffLine = "diff --git a/CoreLibrary/src/main/java/com/caucus/apps/corelib/AppCoreLibModule.java b/CoreLibrary/src/main/java/com/caucus/apps/corelib/Something.jsp";
		ClassChangeRecord changes = new ClassChangeRecord(diffLine, SRC_JAVA_DIR, subProjects);
		assertFalse (changes.isValid());
	}
	
	@Test
	public void shouldNotBeValid_ifTestClass() {
		String diffLine = "diff --git a/CoreLibrary/src/test/java/com/caucus/apps/corelib/MyTest.java b/CoreLibrary/src/test/java/com/caucus/apps/corelib/MyTest.java";
		ClassChangeRecord changes = new ClassChangeRecord(diffLine, SRC_JAVA_DIR, subProjects);
		assertFalse (changes.isValid());
	}
	
	@Test
	public void shouldNotBeValid_ifIntegrationTestClass() {
		String diffLine = "diff --git a/CoreLibrary/src/integration/java/com/caucus/apps/corelib/MyTest.java b/CoreLibrary/src/integration/java/com/caucus/apps/corelib/MyTest.java";
		ClassChangeRecord changes = new ClassChangeRecord(diffLine, SRC_JAVA_DIR, subProjects);
		assertFalse (changes.isValid());
	}

}

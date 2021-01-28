package jp.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConverterUtilTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * �������̕ϊ��@001
	 */
	@Test
	public void chinaNumToArabicNumTestCase001() {

		String rValue = new String();
		String pValue = "�Q�T�U";
		String iValue = "��S�܏\�Z";


		System.out.println("**********Case001 Test START**********");


		rValue = StringUtil.chinaNumToArabicNum(iValue);

		System.out.println("���͒l�F�@" + iValue);
		System.out.println("�z��l�F�@" + pValue);
		System.out.println("�߂�l�F�@" + rValue);

		assertEquals(pValue,rValue);

		System.out.println("**********Case001 Test   END**********");

	}

	/**
	 * �������̕ϊ��@002
	 */
	@Test
	public void chinaNumToArabicNumTestCase002() {

		String rValue = new String();
		String pValue = "�X�X";
		String iValue = "��\��";

		System.out.println();
		System.out.println("**********Case002 Test START**********");

		System.out.println("���͒l�F�@" + iValue);
		System.out.println("�z��l�F�@" + pValue);
		rValue = StringUtil.chinaNumToArabicNum(iValue);
		System.out.println("�߂�l�F�@" + rValue);

		assertEquals(pValue,rValue);

		System.out.println("**********Case002 Test   END**********");

	}

	/**
	 * �������̕ϊ��@003
	 */
	@Test
	public void chinaNumToArabicNumTestCase003() {

		String iValue = "�S�\��";
		String rValue = new String();
		String pValue = "�P�P�X";


		System.out.println();
		System.out.println("**********Case003 Test START**********");




		System.out.println("���͒l�F�@" + iValue);
		System.out.println("�z��l�F�@" + pValue);
		rValue = StringUtil.chinaNumToArabicNum(iValue);
		System.out.println("�߂�l�F�@" + rValue);
		assertEquals(pValue,rValue);

		System.out.println("**********Case003 Test   END**********");

	}


	/**
	 * �������̕ϊ��@004
	 */
	@Test
	public void chinaNumToArabicNumTestCase004() {

		String iValue = "��";
		String pValue = "�Q";
		String rValue = new String();


		System.out.println();
		System.out.println("**********Case004 Test START**********");




		System.out.println("���͒l�F�@" + iValue);
		System.out.println("�z��l�F�@" + pValue);
		rValue = StringUtil.chinaNumToArabicNum(iValue);
		System.out.println("�߂�l�F�@" + rValue);
		assertEquals(pValue,rValue);

		System.out.println("**********Case004 Test   END**********");

	}


	/**
	 * �������̕ϊ��@005
	 */
	@Test
	public void chinaNumToArabicNumTestCase005() {

		String iValue = "��ܘZ";
		String pValue = "�Q�T�U";
		String rValue = new String();


		System.out.println();
		System.out.println("**********Case005 Test START**********");




		System.out.println("���͒l�F�@" + iValue);
		System.out.println("�z��l�F�@" + pValue);
		rValue = StringUtil.chinaNumToArabicNum(iValue);
		System.out.println("�߂�l�F�@" + rValue);
		assertEquals(pValue,rValue);

		System.out.println("**********Case005 Test   END**********");

	}

	/**
	 * �������̕ϊ��@006
	 */
	@Test
	public void chinaNumToArabicNumTestCase006() {

		String iValue = "��܏\��";
		String pValue = "�T�X";
		String rValue = new String();


		System.out.println();
		System.out.println("**********Case006 Test START**********");


		System.out.println("���͒l�F�@" + iValue);
		System.out.println("�z��l�F�@" + pValue);
		rValue = StringUtil.chinaNumToArabicNum(iValue);
		System.out.println("�߂�l�F�@" + rValue);
		assertEquals(pValue,rValue);

		System.out.println("**********Case006 Test   END**********");

	}


}


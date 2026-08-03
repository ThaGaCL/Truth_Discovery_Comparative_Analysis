package qcri.dafna.dataModel.dataFormatter;

import java.util.Date;

import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;

public class DataComparator {

	public static boolean hasSameValue(Object value1, Object value2, ValueType valueType) {
		if ((value1 == null && value2 != null) || (value1 != null && value2 == null)) {
			return false;
		}
		if (value1 == null && value2 == null) {
			return true;// TODO is this right ?
		}
		if ( ! value1.getClass().equals(value2.getClass()))	{
			return false;
		}
		if (valueType.equals(DataTypeMatcher.ValueType.STRING)) {
			return ((String)value1).equalsIgnoreCase((String)value2);
		}
		if (valueType.equals(DataTypeMatcher.ValueType.Name)) {
			if (PersonsNameComparator.sameName((String)value1, (String)value2) > 0) {
				return true;
			}
			return false;
		}
		if (valueType.equals(DataTypeMatcher.ValueType.ListNames)) {
			return sameNames((String)value1, (String) value2);
		}
		return value1.equals(value2);
	}

	public static boolean valueInBucket(Object value, boolean cleanValue, ValueBucket bucket) {
		if (!cleanValue && (bucket.isClean() || !DataTypeMatcher.savedAsString(bucket.getValueType() )) || (cleanValue && !bucket.isClean())) {
			return false;
		}
		if ( ! DataTypeMatcher.savedAsString(bucket.getValueType())) {
			return valueInRange(value, bucket.getMinValue(), bucket.getMaxValue(), bucket.getValueType());
		}
		return hasSameValue(value, bucket.getCleanedString(), bucket.getValueType());
	}

	public static boolean valueInRange(Object value, Object min, Object max, ValueType type) {
		if (type.equals(ValueType.BOOLEAN)) {
			if ( ((Boolean)value && (Boolean)max) || ( !(Boolean)value && !(Boolean)max) ) {
				return true;
			} else {
				return false;
			}
		}
		if (type.equals(ValueType.DATE) || type.equals(ValueType.TIME)) {
			long v, minl, maxl;
			v = ((Date)value).getTime();
			try{
			minl = ((Date)min).getTime();
			} catch (Exception e) {
				
				System.out.println();
				minl = 0;
			}
			maxl = ((Date)max).getTime();
			if (v > minl && v <= maxl ) {
				return true;
			}
			return false;
		}
		if (type.equals(ValueType.LONG) || type.equals(ValueType.FLOAT)) {
			if (Double.valueOf(value.toString()) > Double.valueOf(min.toString()) && Double.valueOf(value.toString()) <= Double.valueOf(max.toString()) ) {
				return true;
			} else if (value.toString().equalsIgnoreCase(min.toString()) || value.toString().equalsIgnoreCase(max.toString())) {
				return true;
			}
			return false;
		}
		return false;
	}

	/**
	 * Checking for the exact names list, regardless of the order.
	 * @param namesList1 The first list of names
	 * @param namesList2 The second list of names
	 * @return True is the two lists contain the same set of names, false otherwise.
	 */
	public static boolean sameNames(String namesList1, String namesList2) {
		return PersonsNameComparator.sameNames(namesList1, namesList2);
	}


	/**
	 * Compute the Implication of the value2 on the value1. i.e. how much does value2 support value1.
	 * @param bucket1 the bucket of value1
	 * @param bucket2 the bucket of value2
	 * @param valueType the value type
	 * @return the implication (influence) from value2 to value1.
	 */
	public static double computeImplication(ValueBucket bucket1, ValueBucket bucket2, ValueType valueType) {
		if (valueType.equals(ValueType.ListNames)) {
			return PersonsNameComparator.computePersonsNamesListSimilarity(bucket1.getCleanedString(), bucket2.getCleanedString());
		}
		if (DataTypeMatcher.savedAsString(valueType)) {
			/** 
			 * the similarity is divided by the length of value2, in order to normalize
			 * and compute the implication of value2 on value1. imp(v2->v1) != imp(v1->v2) 
			 */
			double sim = computeSimilarity(bucket1.getCleanedString(), bucket2.getCleanedString());
			if (bucket2.getCleanedString().length() == 0) {
				return 0.0;
			}
			return sim/(double)bucket2.getCleanedString().length();
		} 
//		if (valueType.equals(ValueType.DATE) || valueType.equals(ValueType.TIME)) {
//			// for unclean values, they will never have the same value (every bucket contains exactly the same values for String)
//			// so no need to compare the string value for the un-cleaned buckets
//			if ( !bucket1.isClean() || !bucket2.isClean()) {
//				return -0.25;
//			}
//			long min1 = ((Date)bucket1.getMinValue()).getTime();
//			long max1 = ((Date)bucket1.getMaxValue()).getTime();
//			long min2 = ((Date)bucket2.getMinValue()).getTime();
//			long max2 = ((Date)bucket2.getMaxValue()).getTime();
//			if (min1 == max2 || min2 == max1) {
//				return 0.25;
//			} else {
//				return -0.25;
//			}
//		}
		return 0.0;
	}

	/**
	 * Compute the string literal similarity between the two given string values.
	 * the used similarity metric is the Jaro Winkler similarity.
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static double computeSimilarity(String s1, String s2) {
		if (s1 == null || s2 == null) {
			return 0.0;
		}
		s1 = s1.trim().toLowerCase();
		s2 = s2.trim().toLowerCase();
		if (s1.equals(s2)) {
			return 1.0;
		}
		int maxLen = Math.max(s1.length(), s2.length());
		if (maxLen == 0) {
			return 1.0;
		}
		int dist = levenshteinDistance(s1, s2);
		return Math.max(0.0, 1.0 - ((double)dist / (double)maxLen));
	}

	private static int levenshteinDistance(String a, String b) {
		int[][] dp = new int[a.length() + 1][b.length() + 1];
		for (int i = 0; i <= a.length(); i++) {
			dp[i][0] = i;
		}
		for (int j = 0; j <= b.length(); j++) {
			dp[0][j] = j;
		}
		for (int i = 1; i <= a.length(); i++) {
			for (int j = 1; j <= b.length(); j++) {
				int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
				dp[i][j] = Math.min(
					Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
					dp[i - 1][j - 1] + cost
				);
			}
		}
		return dp[a.length()][b.length()];
	}
	
}
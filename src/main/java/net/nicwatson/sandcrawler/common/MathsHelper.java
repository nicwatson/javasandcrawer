/*
 *   JavaSandcrawler - A keyword-based demo search engine and web crawler for JavaFX runtimes
 *   Copyright (C) 2022  Nic Watson
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, version 3.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.nicwatson.sandcrawler.common;

/**
 * A helper class containing static methods for mathematical calculations (and debugging)
 * @author Nic
 *
 */
public class MathsHelper
{
	/**
	 * Calculates the base-2 logarithm of a given number 'd'
	 * @param d
	 * @return The base-2 log of 'd'
	 */
	public static double lg(double d)
	{
		// Using the change-of-base rule
		return Math.log(d)/Math.log(2);
	}
	
	/**
	 * Calculates the TF-IDF, given a TF and IDF.
	 * <br/>
	 * <code>tf_idf = log(1 + TF) * IDF</code>
	 * @param tf
	 * @param idf
	 * @return Calculated TF-IDF
	 */
	public static double calcTFIDF(double tf, double idf)
	{
		return MathsHelper.lg(1.0 + tf) * idf;
	}
	
	/**
	 * Calculates the Euclidean distance between two vectors
	 * @param a The first vector, as an array of <b>double</b>
	 * @param b The second vector, as an array of <b>double</b>
	 * @return Euclidean distance between the two vectors
	 */
	
	public static double euclideanDistance(double[] a, double[] b)
	{
		double sum = 0;
		for (int i = 0; i < a.length; i++)
		{
			sum += Math.pow(a[i] - b[i], 2);
		}
		return Math.sqrt(sum);
	}
	
	/**
	 * Calculates the matrix product of a given vector times a given matrix. If the vector is N elements long,
	 * then the matrix must be a square of N x N elements. This could instead be performed with a more generalized
	 * matrix multiplication method (a vector is just a 1D matrix, and it is possible to multiply by a non-square
	 * matrix, etc.), the page rank system only requires this particular special case. Given that page ranking is 
	 * already a processor-intensive task, it makes sense to use a streamlined method that is tailored to the properties
	 * of the special case.
	 * @param a The vector, as an array of <b>double</b>
	 * @param b The matrix, as a two-dimensional array of <b>double</b>
	 * @return The vector result of the multiplication. If the dimensions of the vector and matrix did not match up,
	 * the result will contain all zeroes.
	 */
	public static double[] vectorMultiplySquareMatrix(double[] a, double[][] b)
	{
		
		double[] product = new double[a.length];
		if(a.length == b.length && a.length == b[0].length)
		{
			for (int i = 0; i < a.length; i++)
			{
				product[i] = 0;
				for (int j = 0; j < a.length; j++)
				{
					product[i] += (a[j] * b[j][i]);
				}
			}
		}
		return product;
	}
	
	/**
	 * Debug method for printing and eyeball-checking adjacency matrices
	 * @param matrix
	 */
	public static void print2DByteMatrix(byte[][] matrix)
	{
		for (byte[] element : matrix)
		{
			System.out.println();
			for (int j = 0; j < matrix[0].length; j++)
			{
				if (j > 0)
				{
					System.out.print(",  ");
				}
				System.out.print(element[j]);
			}
		}
		System.out.println();
	}
	
	/**
	 * Debug method for printing and eyeball-checking transition probability matrices
	 * @param matrix
	 */
	public static void print2DDoubleMatrix(double[][] matrix)
	{
		for (double[] element : matrix)
		{
			System.out.println();
			for (int j = 0; j < matrix[0].length; j++)
			{
				if (j > 0)
				{
					System.out.print(",  ");
				}
				System.out.print(element[j]);
			}
		}
		System.out.println();
	}
	
	/**
	 * Debug method for printing and eyeball-checking PageRank vectors
	 * @param matrix
	 */
	public static void printDoubleVector(double[] vector)
	{
		System.out.println();
		System.out.print("[ ");
		for (int i = 0; i < vector.length; i++)
		{
			if (i > 0)
			{
				System.out.print(",  ");
			}
			System.out.print(vector[i]);
		}
		System.out.println(" ]");
	}
}

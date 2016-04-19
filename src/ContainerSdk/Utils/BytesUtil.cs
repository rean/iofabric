using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Utils
{
	public class BytesUtil
	{
		public static byte[] CopyRange(byte[] src, int from, int to)
		{
			byte[] temp = new byte[from];
			byte[] result = new byte[to - from];
			MemoryStream input = new MemoryStream();
			input.Read(temp, 0, temp.Length);
			input.Read(result, 0, result.Length);
			try
			{
				input.Close();
			}
			catch
			{

			}

			return result;
		}

		public static byte[] LongToBytes(long x)
		{
			byte[] b = new byte[8];
			for (int i = 0; i < 8; ++i)
			{
				b[i] = (byte)(x >> (8 - i - 1 << 3));
			}

			return b;
		}

		public static long BytesToLong(byte[] bytes)
		{
			long result = 0;
			for (int i = 0; i < bytes.Length; i++)
			{
				result = (result << 8) + (bytes[i] & 0xff);
			}

			return result;
		}

		public static byte[] IntegerToBytes(int x)
		{
			byte[] b = new byte[4];
			for (int i = 0; i < 4; ++i)
			{
				b[i] = (byte)(x >> (4 - i - 1 << 3));
			}

			return b;
		}

		public static int BytesToInteger(byte[] bytes)
		{
			int result = 0;
			for (int i = 0; i < bytes.Length; i++)
			{
				result = (result << 8) + (bytes[i] & 0xff);
			}

			return result;
		}

		public static byte[] ShortToBytes(short x)
		{
			byte[] b = new byte[2];
			for (int i = 0; i < 2; ++i)
			{
				b[i] = (byte)(x >> (2 - i - 1 << 3));
			}

			return b;
		}

		public static short BytesToShort(byte[] bytes)
		{
			short result = 0;
			for (int i = 0; i < bytes.Length; i++)
			{
				result = (short) ((result << 8) + (bytes[i] & 0xff));
			}

			return result;
		}

		public static byte[] StringToBytes(string x)
		{
			if (x == null)
			{
				return new byte[] { };
			}

			else
			{
				return Encoding.ASCII.GetBytes(x);
			}
		}

		public static string BytesToString(byte[] bytes)
		{
			string s = Encoding.UTF8.GetString(bytes);
			return s;
		}

		public static string ByteArrayToString(byte[] bytes)
		{
			StringBuilder result = new StringBuilder();
			result.Append("[");
			for (int i = 0; i < bytes.Length; i++)
			{
				if (bytes.Length > 1)
				{
					result.Append(", ");
				}

				result.Append(bytes[i]);
			}
			result.Append("]");

			return result.ToString();
		}

		public static int getLength(String s)
		{
			if (s == null)
			{
				return 0;
			}

			else
			{
				return s.Length;
			}
		}
	}
}

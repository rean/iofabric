using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Api.Client
{
	public class Logger
	{
		public void write(String lines)
		{
			System.IO.StreamWriter file = new System.IO.StreamWriter("c:\\message.txt", true);
			file.WriteLine(lines);

			file.Close();
		}
	}
}

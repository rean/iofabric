using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.IO;

namespace IOTracks.IOFabric.ContainerSdk
{
    public class IOMessage
    {
		private const short VERSION = 4;

		public string Id { get; set; }
		public string Tag { get; set; }
		public string MessageGroupId { get; set; }
		public int SequenceNumber { get; set; }
		public int SequenceTotal { get; set; }
		public byte Priority { get; set; }
		public long Timestamp { get; set; }
		public string Publisher { get; set; }
		public string AuthIdentifier { get; set; }
		public string AuthGroup { get; set; }
		public short Version { get; set; }
		public long ChainPosition { get; set; }
		public string Hash { get; set; }
		public string PreviousHash { get; set; }
		public string Nonce { get; set; }
		public int DifficultyTarget { get; set; }
		public string InfoType { get; set; }
		public string InfoFormat { get; set; }
		public byte[] ContextData { get; set; }
		public byte[] ContentData { get; set; }

		public IOMessage()
		{
			Version = VERSION;
		}

		public IOMessage(string publisher)
		{
			this.Publisher = publisher;
		}

		//from json
		public IOMessage(JObject json)
		{
			JToken temp;

			if (json.TryGetValue("id", out temp))
			{
				Id = temp.Value<string>("id");
			}

			if (json.TryGetValue("tag", out temp))
			{
				Tag = temp.Value<string>("tag");
			}

			if (json.TryGetValue("groupid", out temp))
			{
				MessageGroupId = temp.Value<string>("groupid");
			}

			if (json.TryGetValue("sequencenumber", out temp))
			{
				SequenceNumber = temp.Value<int>("sequencenumber");
			}

			if (json.TryGetValue("priority", out temp))
			{
				Priority = temp.Value<byte>("priority");
			}

			if (json.TryGetValue("timestamp", out temp))
			{
				Timestamp = temp.Value<long>("timestamp");
			}

			if (json.TryGetValue("publisher", out temp))
			{
				Publisher = temp.Value<string>("publisher");
			}

			if (json.TryGetValue("authid", out temp))
			{
				AuthIdentifier = temp.Value<string>("authid");
			}

			if (json.TryGetValue("authgroup", out temp))
			{
				AuthGroup = temp.Value<string>("authgroup");
			}

			if (json.TryGetValue("chainposition", out temp))
			{
				ChainPosition = temp.Value<long>("chainposition");
			}

			if (json.TryGetValue("hash", out temp))
			{
				Hash = temp.Value<string>("hash");
			}

			if (json.TryGetValue("previoushash", out temp))
			{
				PreviousHash = temp.Value<string>("previoushash");
			}

			if (json.TryGetValue("nonce", out temp))
			{
				Nonce = temp.Value<string>("nonce");
			}

			if (json.TryGetValue("infotype", out temp))
			{
				InfoType = temp.Value<string>("infotype");
			}

			if (json.TryGetValue("infoformat", out temp))
			{
				InfoFormat = temp.Value<string>("infoformat");
			}

			if (json.TryGetValue("contextdata", out temp))
			{
				ContextData = temp.Value<byte[]>("contextdata");
			}

			if (json.TryGetValue("contentdata", out temp))
			{
				ContentData = temp.Value<byte[]>("contentdata");
			}
		}

		public IOMessage(byte[] rawBytes)
		{
			Version = BitConverter.ToInt16(rawBytes, 0); // 2 bytes long

			if (Version != VERSION)
			{
				// TODO: incompatible version
				return;
			}

			int pos = 33;
			int size = rawBytes[2];

			if (size > 0)
			{
				Id = BitConverter.ToString(rawBytes, pos, size);
				pos += size;
			}

			size = BitConverter.ToInt16(rawBytes, 3); // two bytes long

			if ( size > 0)
			{
				Tag = BitConverter.ToString(rawBytes, pos, size);
				pos += size;
			}

			size = rawBytes[5];
			
			if (size > 0)
			{
				MessageGroupId = BitConverter.ToString(rawBytes, pos, size);
				pos += size;
			}

			size = rawBytes[6];

			if (size > 0)
			{
				SequenceNumber = BitConverter.ToInt32(rawBytes, pos); // 4 bytes long
				pos += size;
			}

			size = rawBytes[7];

			if (size > 0)
			{
				SequenceTotal = BitConverter.ToInt32(rawBytes, pos); // 4 bytes long
				pos += size;
			}

			size = rawBytes[8];

			if (size > 0)
			{
				Priority = rawBytes[pos];
				pos += size;
			}

			size = rawBytes[9];

			if (size > 0)
			{
				Timestamp = BitConverter.ToInt64(rawBytes, pos); // 8 bytes long
				pos += size;
			}

			size = rawBytes[10];

			if (size > 0)
			{
				Publisher = BitConverter.ToString(rawBytes, pos, size);
				pos += size;
			}

			size = BitConverter.ToInt16(rawBytes, 11); // 2 bytes long

			if (size > 0)
			{
				AuthIdentifier = BitConverter.ToString(rawBytes, pos, size);
				pos += size;
			}

			size = BitConverter.ToInt16(rawBytes, 13); // 2 bytes long

			if (size > 0)
			{
				AuthGroup = BitConverter.ToString(rawBytes, pos, size);
				pos += size;
			}

			size = rawBytes[15];

			if (size > 0)
			{
				ChainPosition = BitConverter.ToInt64(rawBytes, pos); // 8 bytes long
				pos += size;
			}

			size = BitConverter.ToInt16(rawBytes, 16); // 2 bytes long

			if (size > 0)
			{
				Hash = BitConverter.ToString(rawBytes, pos, size);
				pos += size;
			}

			size = BitConverter.ToInt16(rawBytes, 18); // 2 bytes long

			if (size > 0)
			{
				PreviousHash = BitConverter.ToString(rawBytes, pos, size);
				pos += size;
			}

			size = BitConverter.ToInt16(rawBytes, 20); // 2 bytes long

			if (size > 0)
			{
				Nonce = BitConverter.ToString(rawBytes, pos, size);
				pos += size;
			}

			size = rawBytes[22];

			if (size > 0)
			{
				DifficultyTarget = BitConverter.ToInt32(rawBytes, pos); // 4 bytes long
				pos += size;
			}

			size = rawBytes[23];

			if (size > 0)
			{
				InfoType = BitConverter.ToString(rawBytes, pos, size);
				pos += size;
			}

			size = rawBytes[24];

			if (size > 0)
			{
				InfoFormat = BitConverter.ToString(rawBytes, pos, size);
				pos += size;
			}

			size = BitConverter.ToInt32(rawBytes, 25);

			if (size > 0)
			{
				int count = 0;
				byte[] temp = new byte[size];

				for (int i = pos; i < size; i++)
				{
					temp[count] = rawBytes[i];
				}

				ContextData = temp;
				pos += size;
			}

			size = BitConverter.ToInt32(rawBytes, 29);

			if (size > 0)
			{
				int count = 0;
				byte[] temp = new byte[size];

				for (int i = pos; i < size; i++)
				{
					temp[count] = rawBytes[i];
				}

				ContentData = temp;
				pos += size;
			}
		}

		public IOMessage(byte[] header, byte[] data)
		{
			Version = BitConverter.ToInt16(header, 0);

			if (Version != VERSION)
			{
				// TODO: incompatible version
				return;
			}

			int pos = 0;
			int size = header[2];

			if (size > 0)
			{
				Id = BitConverter.ToString(data, pos, size);
				pos += size;
			}

			size = BitConverter.ToInt16(header, 3); // 4 bytes long

			if (size > 0)
			{
				Tag = BitConverter.ToString(data, pos, size);
				pos += size;
			}

			size = header[5];

			if ( size > 0)
			{
				MessageGroupId = BitConverter.ToString(data, pos, size);
				pos += size;
			}

			size = header[6];

			if (size > 0)
			{
				SequenceNumber = BitConverter.ToInt32(data, pos); // 4 bytes long
				pos += size;
			}

			size = header[7];

			if ( size > 0)
			{
				SequenceTotal = BitConverter.ToInt32(data, pos); // 4 bytes long
				pos += size;
			}

			size = header[8];

			if (size > 0)
			{
				Priority = data[pos];
				pos += size;
			}

			size = header[9];

			if (size > 0)
			{
				Timestamp = BitConverter.ToInt64(data, pos); // 8 bytes long
				pos += size;
			}

			size = header[10];

			if (size > 0)
			{
				Publisher = BitConverter.ToString(data, pos, size);
				pos += size;
			}

			size = BitConverter.ToInt16(header, 11); // 2 bytes long

			if (size > 0)
			{
				AuthIdentifier = BitConverter.ToString(data, pos, size);
				pos += size;
			}

			size = BitConverter.ToInt16(header, 13);

			if (size > 0)
			{
				AuthGroup = BitConverter.ToString(data, pos, size);
				pos += size;
			}

			size = header[15];

			if (size > 0)
			{
				ChainPosition = BitConverter.ToInt64(data, pos); // 8 bytes long
				pos += size;
			}

			size = BitConverter.ToInt16(header, 16); // 2 bytes long

			if (size > 0)
			{
				Hash = BitConverter.ToString(data, pos, size);
				pos += size;
			}

			size = BitConverter.ToInt16(header, 18); // 2 bytes long

			if (size > 0)
			{
				PreviousHash = BitConverter.ToString(data, pos, size);
				pos += size;
			}

			size = BitConverter.ToInt16(header, 20); //2 bytes long

			if (size > 0)
			{
				Nonce = BitConverter.ToString(data, pos, size);
				pos += size;
			}

			size = header[22];

			if (size > 0)
			{
				DifficultyTarget = BitConverter.ToInt32(data, pos); // 4 bytes long
				pos += size;
			}

			size = header[23];

			if (size > 0)
			{
				InfoType = BitConverter.ToString(data, pos, size);
				pos += size;
			}

			size = header[24];

			if (size > 0)
			{
				InfoFormat = BitConverter.ToString(data, pos, size);
				pos += size;
			}

			size = BitConverter.ToInt32(header, 25); // 4 bytes long

			if (size > 0)
			{
				int count = 0;
				byte[] temp = new byte[size];

				for (int i = pos; i < size; i++)
				{
					temp[count] = data[i];
				}

				ContextData = temp;
			}

			size = BitConverter.ToInt32(header, 29);

			if (size > 0)
			{
				int count = 0;
				byte[] temp = new byte[size];

				for (int i = pos; i < size; i++)
				{
					temp[count] = data[i];
				}

				ContentData = temp;
			}
		}

		private int GetLength(string str)
		{
			int result = 0;
			if (!string.IsNullOrWhiteSpace(str))
			{
				result = str.Length;
			}

			return result;
		}

		public byte[] GetBytes()
		{
			MemoryStream hs = new MemoryStream();
			MemoryStream ds = new MemoryStream();
			int totalL = 0;
			byte[] result = null;

			try
			{
				//Version
				hs.Write(BitConverter.GetBytes((short)Version), 0, 2);

				//id
				int len = GetLength(this.Id);
				hs.WriteByte((byte)(len & 0xFF));

				if (len > 0)
				{
					ds.Write(Encoding.ASCII.GetBytes(Id), totalL, len);
					totalL += len;
				}

				//tag
				len = GetLength(this.Tag);
				hs.WriteByte((byte)(len & 0xFF));

				if (len > 0)
				{
					ds.Write(Encoding.ASCII.GetBytes(Tag), totalL, len);
					totalL += len;
				}

				//groupid
				len = GetLength(this.MessageGroupId);
				hs.WriteByte((byte)(len & 0xFF));

				if (len > 0)
				{
					ds.Write(Encoding.ASCII.GetBytes(MessageGroupId), totalL, len);
					totalL += len;
				}

				//seq no
				if (SequenceNumber == 0)
				{
					hs.WriteByte(0);
				}
				else
				{
					ds.Write(BitConverter.GetBytes(SequenceNumber), totalL, 4);
					totalL += 4;
					hs.WriteByte(4);
				}

				// seq total
				if (SequenceTotal == 0)
				{
					hs.WriteByte(0);
				}
				else
				{
					hs.WriteByte(1);
					ds.WriteByte(Priority);
					totalL += 1;
				}

				//timestamp
				if (Timestamp == 0)
				{
					hs.WriteByte(0);
				}
				else
				{
					hs.WriteByte(8);
					ds.Write(BitConverter.GetBytes(Timestamp), totalL, 8);
					totalL += 8;
				}

				//publisher
				len = GetLength(Publisher);
				hs.WriteByte((byte)(len & 0xff));

				if (len > 0)
				{
					ds.Write(Encoding.ASCII.GetBytes(Publisher), totalL, len);
					totalL += len;
				}

				//authidentifier
				len = GetLength(AuthIdentifier);
				hs.Write(BitConverter.GetBytes((short)(len & 0xffff)), 9, 2);
				if (len > 0)
				{
					ds.Write(Encoding.ASCII.GetBytes(AuthIdentifier), totalL, len);
					totalL += len;
				}

				//authgroup
				len = GetLength(AuthGroup);
				hs.Write(BitConverter.GetBytes((short)(len & 0xffff)), 11, 2);
				if (len > 0)
				{
					ds.Write(Encoding.ASCII.GetBytes(AuthGroup), totalL, len);
					totalL += len;
				}

				//chainposition
				if (ChainPosition == 0)
				{
					hs.WriteByte(0);
				}
				else
				{
					hs.WriteByte(8);
					ds.Write(BitConverter.GetBytes(ChainPosition), totalL, 8);
					totalL += 8;
				}

				//hash
				len = GetLength(Hash);
				hs.Write(BitConverter.GetBytes((short)(len & 0xffff)), 12, 2);
				if (len > 0)
				{
					ds.Write(Encoding.ASCII.GetBytes(Hash), totalL, len);
					totalL += len;
				}

				//PreviousHash
				len = GetLength(PreviousHash);
				hs.Write(BitConverter.GetBytes((short)(len & 0xffff)), 14, 2);
				if (len > 0)
				{
					ds.Write(Encoding.ASCII.GetBytes(PreviousHash), totalL, len);
					totalL += len;
				}

				// nonce
				len = GetLength(Nonce);
				hs.Write(BitConverter.GetBytes((short)(len & 0xffff)), 16, 2);
				if (len > 0)
				{
					ds.Write(Encoding.ASCII.GetBytes(Nonce), totalL, len);
					totalL += len;
				}

				//difficultyTarget
				if (DifficultyTarget == 0)
				{
					hs.WriteByte(0);
				}
				else
				{
					hs.WriteByte(4);
					ds.Write(BitConverter.GetBytes(DifficultyTarget), totalL, 4);
					totalL += 4;
				}

				//Infotype
				len = GetLength(InfoType);
				hs.Write(BitConverter.GetBytes((short)(len & 0xff)), 18, 2);
				if (len > 0)
				{
					ds.Write(Encoding.ASCII.GetBytes(InfoType), totalL, len);
					totalL += len;
				}

				//Infoformat
				len = GetLength(InfoFormat);
				hs.Write(BitConverter.GetBytes((short)(len & 0xff)), 20, 2);
				if (len > 0)
				{
					ds.Write(Encoding.ASCII.GetBytes(InfoFormat), totalL, len);
					totalL += len;
				}

				int x = 22;

				//Contextdata
				if (ContextData == null)
				{
					hs.Write(BitConverter.GetBytes(0), x, 4);
					x += 4;
				}
				else
				{
					len = ContextData.Length;
					hs.Write(BitConverter.GetBytes(len), x, 4);
					x += 4;
					ds.Write(ContextData, totalL, len);
					totalL += len;
				}

				//ContentData
				if (ContentData == null)
				{
					hs.Write(BitConverter.GetBytes(0), x, 4);
					x += 4;
				}
				else
				{
					len = ContentData.Length;
					hs.Write(BitConverter.GetBytes(len), x, 4);
					x += 4;
					ds.Write(ContentData, totalL, len);
					totalL += len;
				}

				result = new byte[hs.Length + ds.Length];
				hs.Write(result, 0, (int)hs.Length);
				ds.Write(result, (int)hs.Length, (int)ds.Length);
			}
			catch
			{
			}
			finally
			{
				try
				{
					hs.Close();
					ds.Close();
				}
				catch
				{
				}
			}

			return result;
		}

		public string JsonToString()
		{
			return ToJson().ToString();
		}

		public JObject ToJson()
		{
			JObject j = new JObject();
			j.Add("id", Id == null ? "" : Id);
			j.Add("tag", Tag == null ? "" : Tag);
            j.Add("groupid", MessageGroupId == null ? "" : MessageGroupId);
            j.Add("sequencenumber", SequenceNumber);
			j.Add("sequencetotal", SequenceTotal);
			j.Add("priority", Priority);
            j.Add("timestamp", Timestamp);
            j.Add("publisher", Publisher == null ? "" : Publisher);
            j.Add("authid", AuthIdentifier == null ? "" : AuthIdentifier);
            j.Add("authgroup", AuthGroup == null ? "" : AuthGroup);
            j.Add("version", Version);
            j.Add("chainposition", ChainPosition);
            j.Add("hash", Hash == null ? "" : Hash);
            j.Add("previoushash", PreviousHash == null ? "" : PreviousHash);
            j.Add("nonce", Nonce == null ? "" : Nonce);
            j.Add("difficultytarget", DifficultyTarget);
            j.Add("infotype", InfoType == null ? "" : InfoType);
            j.Add("infoformat", InfoFormat == null ? "" : InfoFormat);
			j.Add("contextdata", ContextData == null ? "" : new string(System.Text.Encoding.UTF8.GetString(ContextData).ToCharArray()));
			j.Add("contentdata", ContentData == null ? "" : new string(System.Text.Encoding.UTF8.GetString(ContentData).ToCharArray()));
			return j;
		}

		public void DecodeBase64(byte[] bytes)
		{
			string s = Encoding.ASCII.GetString(bytes);
			byte[] rawBytes = Convert.FromBase64String(s);
			IOMessage result = new IOMessage(rawBytes);
			this.AuthGroup = result.AuthGroup;
			this.AuthIdentifier = result.AuthIdentifier;
			this.ChainPosition = result.ChainPosition;
			this.ContentData = result.ContentData;
			this.ContextData = result.ContextData;
			this.DifficultyTarget = result.DifficultyTarget;
			this.Hash = result.Hash;
			this.Id = result.Id;
			this.InfoFormat = result.InfoFormat;
			this.InfoType = result.InfoType;
			this.MessageGroupId = result.MessageGroupId;
			this.Nonce = result.Nonce;
			this.PreviousHash = result.PreviousHash;
			this.Priority = result.Priority;
			this.Publisher = result.Publisher;
			this.SequenceNumber = result.SequenceNumber;
			this.SequenceTotal = result.SequenceTotal;
			this.Tag = result.Tag;
			this.Timestamp = result.Timestamp;
			this.Version = result.Version;
		}

		public byte[] EncodeBase64()
		{
            return Encoding.ASCII.GetBytes(Convert.ToBase64String(this.GetBytes()));
		}
	}
}

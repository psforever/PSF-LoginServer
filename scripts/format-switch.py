import re

def getmap():
  data = open("tmp").read()
  lines = data.split("\n")
  lines_stripped = [l.strip().rstrip() for l in lines]

  datalines = []
  for i in lines_stripped:
    m = re.findall(r'^[A-Z0-9a-z_]+', i)
    if len(m):
      datalines.append(m[0])

  return datalines

def top():
  datalines = getmap()

  for i in range(0, len(datalines), 16):
    print("// OPCODES 0x%02x-%02x" % (i, i+15))
    print("\n".join([d+"," for d in datalines[i:min(i+8, len(datalines))]]))
    print("// 0x%02x" % (i+8))
    print("\n".join([d+"," for d in datalines[min(i+8,len(datalines)):min(i+16, len(datalines))]]))
    print("")

def bot():
  data = open("tmp2").read()
  lines = data.split("\n")
  lines_stripped = [l.strip().rstrip() for l in lines]

  datalinesMap = getmap()
  datalines = []
  for i in lines_stripped:
    m = re.findall(r'^case ([0-9]+)', i)
    if len(m):
      num = int(m[0])
      m = re.findall(r'=> (.*)', i)[0]

      if m.startswith("noDecoder"):
        datalines.append((num, "noDecoder(%s)" % datalinesMap[num]))
      else:
        datalines.append((num, m))

  for i in range(0, len(datalines), 16):
    print("// OPCODES 0x%02x-%02x" % (i, i+15))
    print("\n".join(["case 0x%02x => %s" % (n,d) for n,d in datalines[i:min(i+8, len(datalines))]]))
    print("// 0x%02x" % (i+8))
    print("\n".join(["case 0x%02x => %s" % (n,d) for n,d in datalines[i+8:min(i+16, len(datalines))]]))
    print("")

top()

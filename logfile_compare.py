import argparse

def compare(file1, file2):
	first = open(file1, 'r')
	second = open(file2, 'r')
	lines1 = [lines for lines in first]
	lines2 = [lines for lines in second]

	for i in xrange(0, len(lines1)):
		if lines1[i] != lines2[i]:
			print "not equal:"
			print "line1: "+lines1[i]
			print "line2: "+lines2[i]

def parser():
	parser = argparse.ArgumentParser()
	parser.add_argument("Filename1", help="Target file 1")
	parser.add_argument("Filename2", help="Target file 2")
	args = parser.parse_args()
	returnvalues = []
	returnvalues.append(args.Filename1)
	returnvalues.append(args.Filename2)
	return returnvalues

names = parser()
compare(names[0], names[1])


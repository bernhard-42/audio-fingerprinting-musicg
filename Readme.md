### Add musicg 
Source: [https://github.com/loisaidasam/musicg](https://github.com/loisaidasam/musicg), formerly [https://code.google.com/archive/p/musicg/](https://code.google.com/archive/p/musicg/)

	git clone https://github.com/loisaidasam/musicg.git

### Compile 

	sbt assembly

### Clear and create database (scalikejdbc over h2 database)

	rm -f ./db/*
	./run.sh -c


### Add fingerprint(s) to database

	./run.sh -i <list of PCM 16Bit 44100 stereo wav files>

### Find match

	./run.sh -r <list of PCM 16Bit 44100 stereo wav file chunks>
	
### Dump database

	./run.sh -d
	
### Print fingerprint for a wav chunk

	./run.sh -f <PCM 16Bit 44100 stereo wav file chunks>
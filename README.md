wake
====

wake is a static site generator. The name is a play on make, and the goal
is to upload web sites to S3. Namely, I wanted to make it easier to upload
my site (http://jeffrey.io). So, I wrote a simple tool to push a directory
to S3.

Then, I redesigned my web site and started to add more content. I did an
'incremental' model and simply managed a pile of html files and what not. It was an
exercise in getting things done rather than 'evaluating, comparing, and tweaking'
other frameworks and systems to get what I want.

I realized that I hated html with a passion for writing content, so I slapped in
markdown. Then I started adding navigation, so I added snippets and handler bars.
Not to long after that, I had what you see now.

version 0.2.2 (done)
==================
* universal templates
* snippets/fragments
* she-bang annotations on a file
* per document tagging
* navigation/topology
* very basic language tool integration
* cross page linking
* directory support

version 0.3 (road map)
=====================
* per document table of contents (in progress: 2.1)
* cross document tagging (indexing)
* photo gallery support

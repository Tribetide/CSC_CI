# CSC-virtuaalikoneen pystytys

CSC:n virtuaalikonepalvelun nimi on [Pouta](https://research.csc.fi/service/cpouta-community-cloud-service/).

[Getting started with CSC services for students - Docs CSC](https://docs.csc.fi/support/tutorials/student_quick/)

Follow instructions from Student Project 1-2-3. Add access to service cPouta. And follow instruction from below about cPouta on how to create your own virtual machine and how to set up ssh-access and firewall settings.

Be mindful about following topics

- The student may have one project at a time but be a member of many 
- The student project is access to a fixed amount of resources for a fixed period of time for selected CSC services. 
- So - please be mindful of your use of resources. Respect infrastructure and other users. 


- Create CSC account, https://my.csc.fi/profile (see moodle-course on creating a project first)
- see instructions and login link here: https://research.csc.fi/-/cpouta
    - instructions: https://docs.csc.fi/cloud/pouta/
    - web interface login: https://pouta.csc.fi/dashboard/auth/login/?next=/dashboard/

Goal: Follow instructions, install a VM, access VM using SSH keys.

Once complete, you should be able to access your virtual machine using the command line ssh command available on school computers' powerhell like so (use your own credentials):

```PS C:\Users\k5001199\Documents> ssh -i .\mpan_key.pem ubuntu@193.166.24.35```

Once you have the virtual machine running. Let's instann ngingx web-server. To access the web-server, another firewall rule is required.  Just like with the SSH port 22 (for terminal access), HTTP port 80 needs to be opened from the firewall (to view the web content with a web browser).

To install and enable nginx, run the following commands (see [this link](https://phoenixnap.com/kb/nginx-start-stop-restart) for details):
```
sudo apt-get install nginx # this will use admin priviliges to run apt-get command, and install nginx web-server
sudo systemctl enable nginx # enable the web-server (this makes it start when the os starts up)
sudo systemctl start nginx # start the nginx web-server
```

To host s static web-site on your nginx instance, all you need to do is to place the relevant files in the appropriate nginx-directory. You can start the nano-editor with super user priviliges like so, and edit the spceial index.html file. You can add whatever HTML-content here with the nano editor.
```
cd /var/www/html/
sudo nano index.html
```
Once the aboe is complete, you chould be able to use a web-browset to navigate to website. Use the same floating IP address that you used to connect via ssh. MAke sure that you have the firewall opened for TCP-port 80 also.

Login to [this external moodle course provided by CSC](https://csc.fi/en/training-calendar/csc-computing-environment-self-learning/) to learn more about CSC-services.
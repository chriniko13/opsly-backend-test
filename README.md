```text

  ___                           __
 /                 |           /                   /
(___  ___  ___  ___| ___      (___  ___  ___         ___  ___
|    |___)|___)|   )|___          )|___)|   ) \  )| |    |___)
|    |__  |__  |__/  __/       __/ |__  |      \/ | |__  |__


```

###### Assignee: nick.christidis@yahoo.com

### Description

A client needs to know what is happening on the social networks. All of them. Right now.

The three social networks the client is interested in are:

```text
https://takehome.io/twitter

https://takehome.io/facebook

https://takehome.io/instagram
```


Because these social networks are so webscale, they don't always respond predictably. The delay in their response almost appears like someone waited for a random integer of seconds before responding!

Also, sometimes they will respond with an error. This error will not be valid JSON. Life's hard sometimes.


### Requirements
The client needs to be able to run your thing, then issue the command:

```
curl localhost:3000
```

And get back a JSON response of the output from the three social networks in the format:

```
{ twitter: [tweets], facebook: [statuses], instagram: [photos] }
```

Order isn't important.

This should be a quick little task, but the client is paying us A Billion dollars for it so make sure your implementation is as robust as it is beautiful.

Don't forget to `git push` regularly.

Have fun!


### Run service
* Execute: `mvn spring-boot:run`


### Unit tests
* Execute: `mvn clean test`


### Integration tests (+ unit tests)
* Execute: `mvn clean verify`


### Apply mapping feature (check: `feed-aggregator.apply-mapping` in properties file)
* When set to `true`, then the response is the following:
    ```json
    {
      "facebook": [
        "Here's some photos of my holiday. Look how much more fun I'm having than you are!",
        "I am in a hospital. I will not tell you anything about why I am here."
      ],
      "instagram": [
        
      ],
      "twitter": [
        "If you live to be 100, you should make up some fake reason why, just to mess with people... like claim you ate a pinecone every single day.",
        "STOP TELLING ME YOUR NEWBORN'S WEIGHT AND LENGTH I DON'T KNOW WHAT TO DO WITH THAT INFORMATION."
      ]
    }
    ```
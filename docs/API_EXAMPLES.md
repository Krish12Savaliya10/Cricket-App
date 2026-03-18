# API Examples

## Signup

```http
POST /api/v1/auth/signup
Content-Type: application/json

{
  "fullName": "KRISH SAVALIYA",
  "email": "krish@example.com",
  "password": "Password@123",
  "role": "HOST"
}
```

## Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "krish@example.com",
  "password": "Password@123"
}
```

## Create Team

```http
POST /api/v1/teams
Content-Type: application/json

{
  "hostUserId": 100001,
  "teamName": "INDIA"
}
```

## Add Player

```http
POST /api/v1/teams/200001/players
Content-Type: application/json

{
  "playerName": "VIRAT KOHLI",
  "role": "BATSMAN"
}
```

## Create Single Match

```http
POST /api/v1/matches/single
Content-Type: application/json

{
  "hostUserId": 100001,
  "team1Id": 200001,
  "team2Id": 200002,
  "overs": 2
}
```

## Start Innings

```http
POST /api/v1/matches/300001/innings/1/start
Content-Type: application/json

{
  "battingTeamId": 200001,
  "opener1Id": 400001,
  "opener2Id": 400002,
  "bowlerId": 500001
}
```

## Submit Normal Ball

```http
POST /api/v1/matches/300001/ball
Content-Type: application/json

{
  "eventType": "RUN",
  "runs": 1
}
```

## Submit Wide

```http
POST /api/v1/matches/300001/ball
Content-Type: application/json

{
  "eventType": "WIDE",
  "extraRuns": 1
}
```

## Submit No-Ball With Runs

```http
POST /api/v1/matches/300001/ball
Content-Type: application/json

{
  "eventType": "NO_BALL",
  "noBallMode": "STANDARD",
  "runs": 2
}
```

## Submit Regular Wicket

```http
POST /api/v1/matches/300001/ball
Content-Type: application/json

{
  "eventType": "WICKET",
  "dismissalType": "REGULAR",
  "nextBatsmanId": 400003
}
```

## Submit Runout

```http
POST /api/v1/matches/300001/ball
Content-Type: application/json

{
  "eventType": "WICKET",
  "dismissalType": "RUN_OUT",
  "outPlayerId": 400002,
  "runoutRuns": 1,
  "runoutEnd": "KEEPER_END",
  "nextBatsmanId": 400003
}
```

## Submit Over-Ending Ball

```http
POST /api/v1/matches/300001/ball
Content-Type: application/json

{
  "eventType": "RUN",
  "runs": 0,
  "nextBowlerId": 500002
}
```

## Undo

```http
POST /api/v1/matches/300001/undo-last-ball
```

## Create Series

```http
POST /api/v1/series
Content-Type: application/json

{
  "hostUserId": 100001,
  "seriesName": "INDIA VS AUS",
  "team1Id": 200001,
  "team2Id": 200002,
  "totalMatches": 3,
  "overs": 20
}
```

## Create Tournament

```http
POST /api/v1/tournaments
Content-Type: application/json

{
  "hostUserId": 100001,
  "tournamentName": "WORLD CUP",
  "teamIds": [200001, 200002, 200003, 200004, 200005, 200006],
  "overs": 20,
  "grouped": true,
  "scheduleType": 2
}
```

## Audience Dashboard

```http
GET /api/v1/audience/dashboard
```

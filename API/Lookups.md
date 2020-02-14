Operations
==========

* Add Lookup Type ()

```
GET /lookups
```

```
PUT lookups/code
{
  "name",
  "description",
  "owner",
  "table"
}
```

```
POST /lookups/code/{code} 
{
  "k",
  "v"
  "description"
}

DELETE /lookups/code/{code}/{k}
```

```
POST /lookups/code/{code}?k=""&v=""&description=""
[
  {
    "k",
    "v"
    "description"
  }
]

DELETE /lookups/code/{code}/keys
[
  list of keys
]
```



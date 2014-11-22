jQuery("#simulation")
  .on("click", ".s-8503c4b8-822c-4e6d-a86d-550f84ed9591 .click", function(event, data) {
    var jEvent, jFirer, cases;
    if(data === undefined) { data = event; }
    jEvent = jimEvent(event);
    jFirer = jEvent.getEventFirer();
    if(jFirer.is("#s-Hotspot_1")) {
      cases = [
        {
          "blocks": [
            {
              "actions": [
                {
                  "action": "jimHide",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_1"
                  }
                }
              ]
            }
          ]
        }
      ];
      event.data = data;
      jEvent.launchCases(cases);
    } else if(jFirer.is("#s-Image_5")) {
      cases = [
        {
          "blocks": [
            {
              "actions": [
                {
                  "action": "jimShow",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_4"
                  }
                },
                {
                  "action": "jimShow",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_3"
                  }
                },
                {
                  "action": "jimShow",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_2"
                  }
                }
              ]
            }
          ]
        }
      ];
      event.data = data;
      jEvent.launchCases(cases);
    } else if(jFirer.is("#s-Label_3")) {
      cases = [
        {
          "blocks": [
            {
              "actions": [
                {
                  "action": "jimHide",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_4"
                  }
                },
                {
                  "action": "jimHide",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_3"
                  }
                },
                {
                  "action": "jimHide",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_2"
                  }
                }
              ]
            }
          ]
        }
      ];
      event.data = data;
      jEvent.launchCases(cases);
    } else if(jFirer.is("#s-Label_5")) {
      cases = [
        {
          "blocks": [
            {
              "actions": [
                {
                  "action": "jimHide",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_4"
                  }
                },
                {
                  "action": "jimHide",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_3"
                  }
                },
                {
                  "action": "jimHide",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_2"
                  }
                }
              ]
            }
          ]
        }
      ];
      event.data = data;
      jEvent.launchCases(cases);
    } else if(jFirer.is("#s-Image_8")) {
      cases = [
        {
          "blocks": [
            {
              "actions": [
                {
                  "action": "jimShow",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_7"
                  }
                },
                {
                  "action": "jimShow",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_6"
                  }
                },
                {
                  "action": "jimShow",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_5"
                  }
                }
              ]
            }
          ]
        }
      ];
      event.data = data;
      jEvent.launchCases(cases);
    } else if(jFirer.is("#s-Label_10")) {
      cases = [
        {
          "blocks": [
            {
              "actions": [
                {
                  "action": "jimHide",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_7"
                  }
                },
                {
                  "action": "jimHide",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_6"
                  }
                },
                {
                  "action": "jimHide",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_5"
                  }
                }
              ]
            }
          ]
        }
      ];
      event.data = data;
      jEvent.launchCases(cases);
    } else if(jFirer.is("#s-Label_11")) {
      cases = [
        {
          "blocks": [
            {
              "actions": [
                {
                  "action": "jimHide",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_7"
                  }
                },
                {
                  "action": "jimHide",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_6"
                  }
                },
                {
                  "action": "jimHide",
                  "parameter": {
                    "target": "#s-Dynamic_Panel_5"
                  }
                }
              ]
            }
          ]
        }
      ];
      event.data = data;
      jEvent.launchCases(cases);
    } else if(jFirer.is("#s-Image_4")) {
      cases = [
        {
          "blocks": [
            {
              "actions": [
                {
                  "action": "jimNavigation",
                  "parameter": {
                    "target": "screens/cb71feb5-a011-48b1-b849-90fdf8c98c34"
                  }
                }
              ]
            }
          ]
        }
      ];
      event.data = data;
      jEvent.launchCases(cases);
    } else if(jFirer.is("#s-Label_13")) {
      cases = [
        {
          "blocks": [
            {
              "actions": [
                {
                  "action": "jimNavigation",
                  "parameter": {
                    "target": "screens/cb71feb5-a011-48b1-b849-90fdf8c98c34"
                  }
                }
              ]
            }
          ]
        }
      ];
      event.data = data;
      jEvent.launchCases(cases);
    } else if(jFirer.is("#s-Label_14")) {
      cases = [
        {
          "blocks": [
            {
              "actions": [
                {
                  "action": "jimNavigation",
                  "parameter": {
                    "target": "screens/d12245cc-1680-458d-89dd-4f0d7fb22724",
                    "transition": "slideright"
                  }
                }
              ]
            }
          ]
        }
      ];
      event.data = data;
      jEvent.launchCases(cases);
    }
  });
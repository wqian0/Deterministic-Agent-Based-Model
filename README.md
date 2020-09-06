# Deterministic-Agent-Based-Model

A model for probabilistically predicting the dynamics of outbreaks across static or time-varying contact networks. 

More details on the model and its mathematical formulation can be found [here](https://link.springer.com/chapter/10.1007/978-3-030-50371-0_50).

<img src="https://github.com/wqian0/Deterministic-Agent-Based-Model/blob/master/Agent-Based%20Disease%20Model%202020-09-05%2013-49-36_1.gif" width="500" height="400"/>

Disease spread induced by a contact network generated from University of North Texas class enrollment data.

Stochastic experimental results can be reproduced by using "743" as the SplittableRandom seed and discarding non-outbreak trials (<20% recovered). Results using the Partial Infection Model can be reproduced by enabling first order backflow correction in the function "runTrickleDay()" of the StaticSimulation class. Both sets of experiments were run with vertices.get(0) as the seed of infection.

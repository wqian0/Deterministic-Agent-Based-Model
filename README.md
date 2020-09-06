# Deterministic-Agent-Based-Model

<img src="https://github.com/wqian0/Deterministic-Agent-Based-Model/blob/master/Agent-Based%20Disease%20Model%202020-09-05%2013-49-36_1.gif" width="600" height="400"/>

Disease spread induced by a contact network generated from University of North Texas class enrollment data.

Stochastic experimental results can be reproduced by using "743" as the SplittableRandom seed and discarding non-outbreak trials (<20% recovered). Results using the Partial Infection Model can be reproduced by enabling first order backflow correction in the function "runTrickleDay()" of the StaticSimulation class. Both sets of experiments were run with vertices.get(0) as the seed of infection.

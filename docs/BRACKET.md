## 🧠 Bracket (Single Elimination)

`BracketService.gerarBracket(tournamentId, players)`:

1. **Shuffle** nos players (sem seed na geração atual)
2. Calcula próxima potência de 2 (4, 8, 16...) como tamanho do bracket
3. Insere **byes** (slots `null`) alternadamente
4. Cria todas as partidas (rodadas de 1 a N), `bracket_type=WINNERS`, status `WAITING`
5. Conecta `fk_next_match_win_id` (partida `p` da rodada `r` → partida `p/2` da rodada `r+1`)
6. Preenche primeira rodada; partidas com bye viram `FINISHED` e promovem o adversário
7. Marca como `READY` as partidas seguintes que já têm 2 players

O avanço automático após resultado é feito pelo `MatchService.registrarResultado` (promove winner pro `fk_next_match_win_id`, e finaliza o torneio na última partida).

**Limite atual**: máxima de **16 jogadores** por torneio (validação em `PlayerService.adicionarAoTournament`).
#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../.."

cat <<'EOF'
Three-terminal network validation (server + 2 players)

Terminal 1 (server):
  java -cp classes com.splendor.Main --server

Terminal 2 (player 1):
  nc <server-ip> <port>

Terminal 3 (player 2):
  nc <server-ip> <port>

Why this exists:
  Network validation is only representative when at least one server and two clients
  are connected simultaneously. Running only one terminal does not validate gameplay flow.
EOF

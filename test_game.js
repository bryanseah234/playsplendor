const { spawn } = require('child_process');

const p = spawn('java', ['-cp', 'classes', 'com.splendor.Main']);
p.stdout.on('data', (data) => {
    const s = data.toString();
    process.stdout.write(s);
    if (s.includes('Enter number of players (2-4):')) {
        p.stdin.write('2\n');
    } else if (s.includes('Enter name for Player 1:')) {
        p.stdin.write('Alice\n');
    } else if (s.includes('Enter name for Player 2:')) {
        p.stdin.write('Bob\n');
    } else if (s.includes('Goal: 15 points')) {
        // give it a moment to print the layout then quit
        setTimeout(() => process.exit(0), 1000);
    }
});
p.stderr.on('data', (data) => {
    console.error(data.toString());
});
